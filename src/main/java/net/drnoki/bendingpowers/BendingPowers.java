package net.drnoki.bendingpowers;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.drnoki.bendingpowers.entity.custom.BoulderEntity;
import net.drnoki.bendingpowers.entity.custom.EarthSpikeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BendingPowers implements ModInitializer {
    public static final String MOD_ID = "bendingpowers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Gesture and Spawning Storage
    private static final Map<UUID, GestureData> SPIKE_GESTURES = new HashMap<>();
    private static final List<PendingSpike> PENDING_SPIKES = new ArrayList<>();

    @Override
    public void onInitialize() {
        ModEntities.registerModEntities();

        // --- GESTURE START & BOULDER SPAWN ---
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand != Hand.MAIN_HAND || !player.getMainHandStack().isEmpty()) return ActionResult.PASS;
            if (!player.getCommandTags().contains("earth_power") || !player.isSneaking()) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.SUCCESS;

            // --- 1. EARTH SPIKE GESTURE (Looking sharply down) ---
            // If looking down more than 40 degrees, only start the gesture and STOP
            if (player.getPitch() > 40f) {
                SPIKE_GESTURES.put(player.getUuid(), new GestureData(player.getPitch(), player.getYaw()));
                // Return success here so the code below (boulder) never runs
                return ActionResult.SUCCESS;
            }

            // --- 2. BOULDER SPAWNING (Looking more towards horizon) ---
            boolean hasBoulder = !world.getEntitiesByType(ModEntities.BOULDER,
                    player.getBoundingBox().expand(64), b -> player.equals(b.getOwner())).isEmpty();

            if (!hasBoulder) {
                BlockPos blockPos = hitResult.getBlockPos();
                Vec3d spawnPos = Vec3d.ofCenter(blockPos).add(0, 1.0, 0);
                BoulderEntity boulder = new BoulderEntity(ModEntities.BOULDER, world);
                boulder.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
                boulder.setOwner(player);
                world.spawnEntity(boulder);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });

        // --- BOULDER LAUNCH ---
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> handleBoulderLaunch(player, world, hand));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> handleBoulderLaunch(player, world, hand));

        // --- GESTURE TICKING & SPIKE PROCESSING ---
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            MinecraftServer server = serverWorld.getServer();

            // Track Swipes
            SPIKE_GESTURES.entrySet().removeIf(entry -> {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player == null) return true;

                GestureData gesture = entry.getValue();
                gesture.ticks++;

                // Trigger if player swiped upward by 45+ degrees quickly
                float pitchChange = gesture.startPitch - player.getPitch();
                if (pitchChange >= 45f && gesture.ticks < 15) {
                    queueEarthSpikes(player, serverWorld, gesture);
                    return true;
                }
                return gesture.ticks > 20; // Expire after 1 second
            });

            // Process Pending Spikes (Delayed Spawning)
            // Only process spikes belonging to THIS dimension so multi-world ticking
            // doesn't decrement (and consume) spikes meant for a different world.
            PENDING_SPIKES.removeIf(pending -> {
                if (!pending.dimension.equals(serverWorld.getRegistryKey())) return false;

                pending.ticksRemaining--;
                if (pending.ticksRemaining > 0) return false;

                EarthSpikeEntity spike = new EarthSpikeEntity(ModEntities.EARTH_SPIKE, serverWorld);
                spike.setPosition(pending.pos.x, pending.pos.y, pending.pos.z);
                // Apply the player's yaw so the spike faces the direction they were looking
                spike.setYaw(pending.yaw);
                spike.setHeadYaw(pending.yaw);
                spike.setOwnerUuid(pending.playerUuid);
                serverWorld.spawnEntity(spike);

                // Line sweep between spikes using the stored facing direction
                Box lineSweep = new Box(pending.pos, pending.pos.add(pending.facing.multiply(2.5)))
                        .expand(0.5, 0.5, 0.5);

                serverWorld.getOtherEntities(spike, lineSweep, e ->
                        e instanceof LivingEntity && !e.getUuid().equals(pending.playerUuid)
                ).forEach(e -> e.damage(serverWorld, serverWorld.getDamageSources().generic(), 4.0f));

                return true;
            });
        });
    }

    private static void queueEarthSpikes(ServerPlayerEntity player, ServerWorld world, GestureData gesture) {
        Vec3d facing = Vec3d.fromPolar(0, gesture.yaw);

        for (int i = 0; i < 3; i++) {
            double distance = 3.5 + (i * 3.5);
            Vec3d targetPos = player.getEntityPos().add(facing.multiply(distance));
            Vec3d groundPos = findGround(world, targetPos);

            if (groundPos != null) {
                // Pass gesture.yaw so each spike is oriented to match the player's facing direction
                PENDING_SPIKES.add(new PendingSpike(player.getUuid(), world.getRegistryKey(), groundPos, facing, gesture.yaw, i * 7));
            }
        }
    }

    @Nullable
    private static Vec3d findGround(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        for (int dy = 10; dy >= -10; dy--) {
            BlockPos check = blockPos.add(0, dy, 0);
            // Use !isAir() instead of isSolidBlock() so slabs, paths, etc. all count as ground
            if (!world.getBlockState(check).isAir() && world.getBlockState(check.up()).isAir()) {
                return Vec3d.ofCenter(check.up());
            }
        }
        return null;
    }

    private static ActionResult handleBoulderLaunch(PlayerEntity player, World world, Hand hand) {
        if (hand != Hand.MAIN_HAND || !player.getMainHandStack().isEmpty()) return ActionResult.PASS;
        if (!player.getCommandTags().contains("earth_power") || world.isClient()) return ActionResult.PASS;

        List<BoulderEntity> boulders = world.getEntitiesByType(ModEntities.BOULDER,
                player.getBoundingBox().expand(20), b -> player.equals(b.getOwner()));

        if (!boulders.isEmpty()) {
            BoulderEntity boulder = boulders.get(0);
            boulder.setVelocity(player.getRotationVec(1.0f).multiply(2.5));
            boulder.velocityDirty = true;
            // Set state to flying (1)
            boulder.getDataTracker().set(BoulderEntity.STATE, (byte) 1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    // --- Helper Classes ---
    private static class GestureData {
        final float startPitch;
        final float yaw;
        int ticks = 0;
        GestureData(float startPitch, float yaw) { this.startPitch = startPitch; this.yaw = yaw; }
    }

    private static class PendingSpike {
        final UUID playerUuid;
        final RegistryKey<World> dimension;
        final Vec3d pos;
        final Vec3d facing;
        final float yaw; // stored so the spike entity can be rotated to match
        int ticksRemaining;

        PendingSpike(UUID playerUuid, RegistryKey<World> dimension, Vec3d pos, Vec3d facing, float yaw, int delay) {
            this.playerUuid = playerUuid;
            this.dimension = dimension;
            this.pos = pos;
            this.facing = facing;
            this.yaw = yaw;
            this.ticksRemaining = delay;
        }
    }
}