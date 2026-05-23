package net.drnoki.bendingpowers.entity.custom;

import net.drnoki.bendingpowers.sound.ModSounds;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EarthSpikeEntity extends Entity {
    private static final int EMERGE_TICKS = 15;
    private static final int LIFETIME_TICKS = EMERGE_TICKS + 50;
    private static final float DAMAGE = 8.0f;
    private static final float AREA_RADIUS = 1.5f;

    public final AnimationState emergeAnimationState = new AnimationState();
    private boolean hasDamaged = false;
    @Nullable private UUID ownerUuid;

    public EarthSpikeEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        if (world.isClient()) {
            emergeAnimationState.start(0);
        }
    }

    public void setOwnerUuid(@Nullable UUID uuid) { this.ownerUuid = uuid; }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getEntityWorld().isClient()) {
            return;
        }

        if (this.age == 1) {
            this.getEntityWorld().playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    ModSounds.EARTH_SPIKE_EMERSION,
                    SoundCategory.HOSTILE,
                    1.0f,
                    1.0f
            );
        }

        if (this.age == EMERGE_TICKS && !hasDamaged) {
            damageNearby();
            hasDamaged = true;
        }

        if (this.age >= LIFETIME_TICKS) this.discard();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    private void damageNearby() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Box damageBox = this.getBoundingBox().expand(AREA_RADIUS);

        this.getEntityWorld().getOtherEntities(this, damageBox,
                e -> e instanceof LivingEntity && !e.getUuid().equals(ownerUuid)
        ).forEach(e -> {
            e.damage(serverWorld, serverWorld.getDamageSources().magic(), DAMAGE);
        });
    }


    @Override
    public void writeCustomData(WriteView view) {
        // We use the built-in UUID codec to tell the view how to handle the object
        view.putNullable("Owner", net.minecraft.util.Uuids.INT_STREAM_CODEC, ownerUuid);
    }

    @Override
    public void readCustomData(ReadView view) {
        // We get the value using the same codec.
        // If the ReadView returns an Optional, we can use .orElse(null)
        this.ownerUuid = view.read("Owner", net.minecraft.util.Uuids.INT_STREAM_CODEC).orElse(null);
    }
}