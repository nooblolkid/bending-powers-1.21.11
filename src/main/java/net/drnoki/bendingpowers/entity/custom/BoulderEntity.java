package net.drnoki.bendingpowers.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BoulderEntity extends ProjectileEntity {
    public static final TrackedData<Byte> STATE = DataTracker.registerData(BoulderEntity.class, TrackedDataHandlerRegistry.BYTE);
    private int hitCount = 0;
    private final Set<UUID> hitEntities = new HashSet<>();
    private int blocksBroken = 0;
    private static final float BLAST_RESISTANCE = 3.0f;
    private static final int MAX_BLOCK_BREAKS = 40;

    public final AnimationState formationAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    private static final int FORMATION_TICKS = 70;

    public BoulderEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(STATE, (byte) 0); // Start in Formation state
    }

    public byte getState() {
        return this.dataTracker.get(STATE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            byte state = this.dataTracker.get(STATE);
            if (state == 0 && this.age > FORMATION_TICKS) {
                this.dataTracker.set(STATE, (byte) 2); // done forming, hover and wait for launch
            } else if (state == 2 && this.getVelocity().lengthSquared() >= 0.01) {
                this.dataTracker.set(STATE, (byte) 1); // launched, now flying
            } else if (state == 1 && this.getVelocity().lengthSquared() < 0.01) {
                this.dataTracker.set(STATE, (byte) 2); // stopped, back to idle
            }
        }

        if (this.dataTracker.get(STATE) == 1) {
            Vec3d vec3d = this.getVelocity();

            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.hitOrDeflect(hitResult);

            double d = this.getX() + vec3d.x;
            double e = this.getY() + vec3d.y;
            double f = this.getZ() + vec3d.z;

            this.updateRotation();

            if (this.isTouchingWater()) {
                this.discard();
            } else {
                this.setVelocity(vec3d.multiply(0.99F));
                this.applyGravity();
                this.setPosition(d, e, f); // move FIRST

                // Now check bounding box at the new position
                if (!this.getEntityWorld().isClient() && !this.isRemoved()) {
                    boolean hitHardBlock = false;
                    Box box = this.getBoundingBox();
                    BlockPos.Mutable mutablePos = new BlockPos.Mutable();

                    outer:
                    for (int x = (int) Math.floor(box.minX); x <= (int) Math.floor(box.maxX); x++) {
                        for (int y = (int) Math.floor(box.minY); y <= (int) Math.floor(box.maxY); y++) {
                            for (int z = (int) Math.floor(box.minZ); z <= (int) Math.floor(box.maxZ); z++) {
                                mutablePos.set(x, y, z);
                                BlockState blockState = this.getEntityWorld().getBlockState(mutablePos);

                                if (blockState.isAir()) continue;

                                float blastResistance = blockState.getBlock().getBlastResistance();
                                boolean isSoft = blastResistance >= 0 && blastResistance <= BLAST_RESISTANCE;

                                if (isSoft && blocksBroken < MAX_BLOCK_BREAKS) {
                                    this.getEntityWorld().breakBlock(mutablePos.toImmutable(), true, this, 512);
                                    blocksBroken++;
                                } else {
                                    hitHardBlock = true;
                                    break outer;
                                }
                            }
                        }
                    }

                    if (hitHardBlock || blocksBroken >= MAX_BLOCK_BREAKS) {
                        this.explodeAndDiscard();
                    }
                }
            }
        }

        // Client-side animation failsafe
        if (this.getEntityWorld().isClient()) {
            if (this.age == 1 && this.dataTracker.get(STATE) == 0) {
                this.formationAnimationState.start(this.age);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        // This is exactly how the Warden triggers its roar/emerge animations!
        if (STATE.equals(data)) {
            System.out.println("Client: State changed to " + this.dataTracker.get(STATE));
            byte state = this.dataTracker.get(STATE);
            switch (state) {
                case 0 -> {
                    this.formationAnimationState.start(this.age);
                    this.idleAnimationState.stop();
                }
                case 1 -> {
                    // Flying (You can add a flyingAnimationState later if you want)
                    this.formationAnimationState.stop();
                    this.idleAnimationState.start(this.age);
                }
                case 2 -> {
                    this.formationAnimationState.stop();
                    this.idleAnimationState.start(this.age);
                }
            }
        }
        super.onTrackedDataSet(data);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (target == owner) return;

        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            target.damage(serverWorld, this.getDamageSources().thrown(this, owner), 6.0F);
        }

        hitEntities.add(target.getUuid());
        hitCount++;

        if (hitCount >= 5) {
            this.explodeAndDiscard();
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !hitEntities.contains(entity.getUuid());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (this.getEntityWorld().isClient()) return;

        BlockState blockState = this.getEntityWorld().getBlockState(blockHitResult.getBlockPos());
        float blastResistance = blockState.getBlock().getBlastResistance();
        boolean isSoft = blastResistance >= 0 && blastResistance <= BLAST_RESISTANCE;

        if (isSoft && blocksBroken < MAX_BLOCK_BREAKS) {
            this.getEntityWorld().breakBlock(blockHitResult.getBlockPos(), true, this, 512);
            blocksBroken++;
        } else {
            this.explodeAndDiscard();
        }
    }

    private void explodeAndDiscard() {
        // Play a sound
        this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLOCK_DEEPSLATE_BREAK,
                net.minecraft.sound.SoundCategory.NEUTRAL, 10.0F, 0.5F);

        // Summon some particles (Server-side)
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, getLandingBlockState()),
                    this.getX(), this.getY(), this.getZ(), 50, 1, 1, 1, 0.5);
        }

        // Remove the entity
        this.discard();
    }


    @Override
    protected double getGravity() {
        return 0.03;
    }
}
