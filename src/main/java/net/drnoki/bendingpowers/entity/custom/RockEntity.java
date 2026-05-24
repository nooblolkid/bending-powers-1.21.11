package net.drnoki.bendingpowers.entity.custom;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RockEntity extends ProjectileEntity {

    // ── Tuning ──────────────────────────────────────────────────────────
    private static final int    RISE_TICKS   = 18;   // ticks to rise from ground
    private static final double LAUNCH_SPEED = 1.8;  // blocks per tick when fired
    private static final float  DAMAGE       = 1.0f; // half-hearts on hit
    private static final double HIT_RADIUS   = 1.5;  // aoe radius at target pos
    private static final double ARRIVE_DIST  = 0.8;  // "close enough" threshold


    // ── State machine ───────────────────────────────────────────────────
    public enum State { RISING, IDLE, LAUNCHED }

    private State state      = State.RISING;
    private int   riseTick   = 0;
    private int   spawnDelay = 0; // ticks to wait before rising (stagger)

    // ── Positions (all in world space) ───────────────────────────────────
    private Vec3d scatterPos = null; // randomised ground start point
    private Vec3d idlePos    = null; // fixed hover position in the ring
    private Vec3d targetPos  = null; // where it flies when launched

    private static final TrackedData<Float> IDLE_X =
            DataTracker.registerData(RockEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> IDLE_Y =
            DataTracker.registerData(RockEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> IDLE_Z =
            DataTracker.registerData(RockEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Byte> ROCK_STATE =
            DataTracker.registerData(RockEntity.class, TrackedDataHandlerRegistry.BYTE);

    // ── Register them in initDataTracker ─────────────────────────────────
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(IDLE_X,     0f);
        builder.add(IDLE_Y,     0f);
        builder.add(IDLE_Z,     0f);
        builder.add(ROCK_STATE, (byte) 0); // 0=RISING, 1=IDLE, 2=LAUNCHED
    }

    // ── Helper: read idlePos back from tracker (safe on client) ──────────
    private Vec3d getTrackedIdlePos() {
        return new Vec3d(
                this.dataTracker.get(IDLE_X),
                this.dataTracker.get(IDLE_Y),
                this.dataTracker.get(IDLE_Z)
        );
    }

    // ── Write idlePos into tracker whenever it's set ─────────────────────
    // Call this in your spawn constructor after setting idlePos:
    private void syncIdlePos() {
        this.dataTracker.set(IDLE_X, (float) idlePos.x);
        this.dataTracker.set(IDLE_Y, (float) idlePos.y);
        this.dataTracker.set(IDLE_Z, (float) idlePos.z);
    }

    // ── Update ROCK_STATE tracker whenever state changes ─────────────────
    private void syncState() {
        byte b = switch (state) {
            case RISING   -> (byte) 0;
            case IDLE     -> (byte) 1;
            case LAUNCHED -> (byte) 2;
        };
        this.dataTracker.set(ROCK_STATE, b);
    }

    // ── Constructors ─────────────────────────────────────────────────────

    /** Deserialization – required by EntityType. */
    public RockEntity(EntityType<? extends ProjectileEntity> type, World world) {
        super(type, world);
    }

    /**
     * Ability spawn constructor.
     *
     * @param ringPos     The exact XZ position on the ring, at ground Y.
     *                    Pass {@code owner.getY()} as the Y – the entity
     *                    will add the idle height offset itself.
     * @param idleHeight  How high above ringPos.y the rock should hover.
     * @param spawnDelay  Ticks to wait before rising (use index * 2 for stagger).
     */
    public RockEntity(
            World world,
            LivingEntity owner,
            Vec3d ringPos,
            double idleHeight,
            int spawnDelay
    ) {
        super(ModEntities.ROCK, world);
        this.setOwner(owner);
        this.spawnDelay = spawnDelay;

        // Idle position: exact ring spot, elevated
        this.idlePos = new Vec3d(ringPos.x, ringPos.y + idleHeight, ringPos.z);
        syncIdlePos(); // ← add this
        syncState();

        // Scatter: random horizontal offset that converges to idlePos during rise
        double sx = ringPos.x + (world.getRandom().nextDouble() - 0.5) * 2.0;
        double sz = ringPos.z + (world.getRandom().nextDouble() - 0.5) * 2.0;
        this.scatterPos = new Vec3d(sx, ringPos.y, sz);

        this.setNoGravity(true);
        this.setPosition(scatterPos.x, scatterPos.y, scatterPos.z);
    }

    // ── Tick ─────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        if (spawnDelay > 0) {
            spawnDelay--;
            return; // sit on the ground until our turn
        }

        switch (state) {
            case RISING   -> tickRising();
            case IDLE     -> tickIdle();
            case LAUNCHED -> tickLaunched();
        }
    }

    private void tickRising() {
        // Get idlePos from the tracker – works on both server AND client
        Vec3d idle = getTrackedIdlePos();

        // If tracker hasn't been populated yet (shouldn't happen but safe guard)
           if (idle.x == 0 && idle.y == 0 && idle.z == 0) {
                   this.discard();
                   return;
           }


        riseTick++;

        if (riseTick >= RISE_TICKS) {
            this.setPosition(idle.x, idle.y, idle.z);
            this.setVelocity(Vec3d.ZERO);
            state = State.IDLE;
            syncState(); // ← tell client we're now IDLE
            return;
        }

        float t     = (float) riseTick / RISE_TICKS;
        float eased = easeOutCubic(t);

        // On the client scatterPos may be null – fall back to idle position
        // so the rock at least appears at the right spot even without scatter animation
        Vec3d scatter = scatterPos != null ? scatterPos : idle;

        double x = scatter.x + (idle.x - scatter.x) * eased;
        double z = scatter.z + (idle.z - scatter.z) * eased;
        double y = scatter.y + (idle.y - scatter.y) * easeOutBack(t);

        this.setVelocity(Vec3d.ZERO);
        this.setPosition(x, y, z);
    }

    private void tickIdle() {
        // Koristimo isključivo poziciju iz trackera jer je 'idlePos' null na klijentskoj strani
        Vec3d idle = getTrackedIdlePos();

        this.setVelocity(Vec3d.ZERO);
        this.setPosition(idle.x, idle.y, idle.z);
    }

    private void tickLaunched() {
        if (targetPos == null) {
            // Client doesn't know the target yet — just hold position until it does
            this.setVelocity(Vec3d.ZERO);
            return;
        }

        Vec3d toTarget = targetPos.subtract(this.getEntityPos());
        if (toTarget.length() < ARRIVE_DIST) {
            onArriveAtTarget();
            return;
        }

        this.setVelocity(toTarget.normalize().multiply(LAUNCH_SPEED));
        this.setNoGravity(true);

        // super.tick() moves the entity by velocity and fires onEntityHit / onBlockHit
        super.tick();

        HitResult hit = ProjectileUtil.getCollision(this, this::canHit);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onCollision(hit);
        }
    }

    // ── Easing functions ─────────────────────────────────────────────────

    /** Decelerates to a smooth stop. */
    private static float easeOutCubic(float t) {
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    /** Overshoots then settles – gives the Y a satisfying "pop" at the top. */
    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
    }

    // ── Collision callbacks ───────────────────────────────────────────────

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (this.getEntityWorld().isClient()) return;
        net.minecraft.entity.Entity target = hit.getEntity();
        if (target == this.getOwner()) return;
        DamageSource src = this.getDamageSources().thrown(this, this.getOwner());
        target.damage((ServerWorld) this.getEntityWorld(), src, DAMAGE);
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        if (!this.getEntityWorld().isClient()) this.discard();
    }

    private void onArriveAtTarget() {
        if (this.getEntityWorld().isClient()) return;
        ServerWorld  sw  = (ServerWorld) this.getEntityWorld();
        DamageSource src = this.getDamageSources().thrown(this, this.getOwner());
        sw.getEntitiesByClass(
                LivingEntity.class,
                new Box(targetPos, targetPos).expand(HIT_RADIUS),
                e -> e != this.getOwner() && e.isAlive()
        ).forEach(e -> e.damage(sw, src, DAMAGE));
        this.discard();
    }

    @Override
    protected boolean canHit(net.minecraft.entity.Entity entity) {
        return super.canHit(entity) && entity != this.getOwner();
    }

    // ── Public API ────────────────────────────────────────────────────────

    public void launch(Vec3d target) {
        this.targetPos = target;
        this.state     = State.LAUNCHED;
    }

    public State getState() {
        return switch (this.dataTracker.get(ROCK_STATE)) {
            case 1  -> State.IDLE;
            case 2  -> State.LAUNCHED;
            default -> State.RISING;
        };
    }

    // ── Serialization ─────────────────────────────────────────────────────

    /** Helper: write a nullable Vec3d as three doubles under a named prefix. */
    private static void writeVec3d(WriteView view, String prefix, Vec3d v) {
        if (v == null) return;
        view.putDouble(prefix + "X", v.x);
        view.putDouble(prefix + "Y", v.y);
        view.putDouble(prefix + "Z", v.z);
    }

    /** Helper: read a nullable Vec3d – returns null if the prefix isn't present. */
    private static Vec3d readVec3d(ReadView view, String prefix) {
        if (!view.contains(prefix + "X")) return null;
        return new Vec3d(
                view.getDouble(prefix + "X", 0.0),
                view.getDouble(prefix + "Y", 0.0),
                view.getDouble(prefix + "Z", 0.0)
        );
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);

        view.putString("rockState",  state.name());
        view.putInt("riseTick",    riseTick);
        view.putInt("spawnDelay",  spawnDelay);

        writeVec3d(view, "scatter", scatterPos);
        writeVec3d(view, "idle",    idlePos);
        writeVec3d(view, "target",  targetPos);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);

        state      = State.valueOf(view.getString("rockState", State.RISING.name()));
        riseTick   = view.getInt("riseTick",   0);
        spawnDelay = view.getInt("spawnDelay", 0);

        scatterPos = readVec3d(view, "scatter");
        idlePos    = readVec3d(view, "idle");
        targetPos  = readVec3d(view, "target");
    }
}
