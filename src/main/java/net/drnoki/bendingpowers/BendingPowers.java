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
            PENDING_SPIKES.removeIf(pending -> {
                pending.ticksRemaining--;
                if (pending.ticksRemaining > 0) return false;

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(pending.playerUuid);
                EarthSpikeEntity spike = new EarthSpikeEntity(ModEntities.EARTH_SPIKE, serverWorld);

                // --- APPLY ROTATION HERE ---
                spike.setPosition(pending.pos.x, pending.pos.y, pending.pos.z);
                spike.setYaw(pending.yaw);
                spike.lastYaw = pending.yaw; // Prevents the model from "snapping" from North

                spike.setOwnerUuid(pending.playerUuid);
                serverWorld.spawnEntity(spike);

                // ... (rest of your damage bridge logic) ...
                return true;
            });
        });
    }

    private static void queueEarthSpikes(ServerPlayerEntity player, ServerWorld world, GestureData gesture) {
        Vec3d facing = Vec3d.fromPolar(0, gesture.yaw);
        Vec3d lastPos = null;

        for (int i = 0; i < 4; i++) {
            // Distance: 3.0, 6.0, 9.0 blocks ahead
            double distance = 3.0 + (i * 3.0);
            Vec3d targetPos = player.getEntityPos().add(facing.multiply(distance));

            Vec3d groundPos = findGround(world, targetPos);

            if (groundPos != null) {
                // Pass the gesture.yaw to each spike
                PENDING_SPIKES.add(new PendingSpike(player.getUuid(), groundPos, i * 5, lastPos, gesture.yaw));
                lastPos = groundPos;
            }
        }
    }

    @Nullable
    private static Vec3d findGround(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        // Expand search to 10 blocks up/down to handle 1.21 terrain generation better
        for (int dy = 10; dy >= -10; dy--) {
            BlockPos check = blockPos.add(0, dy, 0);
            if (world.getBlockState(check).isSolidBlock(world, check) && world.getBlockState(check.up()).isAir()) {
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
        final Vec3d pos;
        final Vec3d previousPos;
        final float yaw; // Added: Stores the rotation
        int ticksRemaining;

        PendingSpike(UUID playerUuid, Vec3d pos, int delay, @Nullable Vec3d previousPos, float yaw) {
            this.playerUuid = playerUuid;
            this.pos = pos;
            this.ticksRemaining = delay;
            this.previousPos = previousPos;
            this.yaw = yaw; // Assign the rotation
        }
    }
}