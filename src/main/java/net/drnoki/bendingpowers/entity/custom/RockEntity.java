package net.drnoki.bendingpowers.entity.custom;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
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
    private static final float  DAMAGE       = 6.0f; // half-hearts on hit
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
    protected RockEntity(
            World        world,
            LivingEntity owner,
            Vec3d        ringPos,
            double       idleHeight,
            int          spawnDelay
    ) {
        super(ModEntities.ROCK, world);
        this.setOwner(owner);
        this.spawnDelay = spawnDelay;

        // Idle position: exact ring spot, elevated
        this.idlePos = new Vec3d(ringPos.x, ringPos.y + idleHeight, ringPos.z);

        // Scatter: random horizontal offset that converges to idlePos during rise
        double sx = ringPos.x + (world.getRandom().nextDouble() - 0.5) * 2.0;
        double sz = ringPos.z + (world.getRandom().nextDouble() - 0.5) * 2.0;
        this.scatterPos = new Vec3d(sx, ringPos.y, sz);

        this.setNoGravity(true);
        this.setPosition(scatterPos.x, scatterPos.y, scatterPos.z);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) { }

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
        riseTick++;

        if (riseTick >= RISE_TICKS) {
            // Snap exactly to idle pos and stop
            this.setPosition(idlePos.x, idlePos.y, idlePos.z);
            this.setVelocity(Vec3d.ZERO);
            state = State.IDLE;
            return;
        }

        float t      = (float) riseTick / RISE_TICKS;
        float eased  = easeOutCubic(t);

        // X/Z: scatter start → idle ring position
        double x = scatterPos.x + (idlePos.x - scatterPos.x) * eased;
        double z = scatterPos.z + (idlePos.z - scatterPos.z) * eased;

        // Y: ground level → idle height, with a slight overshoot then settle
        double y = scatterPos.y + (idlePos.y - scatterPos.y) * easeOutBack(t);

        this.setVelocity(Vec3d.ZERO);
        this.setPosition(x, y, z);
    }

    private void tickIdle() {
        // Rock stays exactly at its assigned ring position.
        // We still call setPosition every tick so packet updates keep clients in sync.
        this.setVelocity(Vec3d.ZERO);
        this.setPosition(idlePos.x, idlePos.y, idlePos.z);

        // TODO: idle bob/rotation animation hook goes here later
    }

    private void tickLaunched() {
        if (targetPos == null) { this.discard(); return; }

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

    public State getState() { return state; }

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
