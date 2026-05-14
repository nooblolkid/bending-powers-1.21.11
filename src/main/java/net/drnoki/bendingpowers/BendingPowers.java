package net.drnoki.bendingpowers;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.drnoki.bendingpowers.entity.custom.BoulderEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BendingPowers implements ModInitializer {

    // Very important comment
	public static final String MOD_ID = "bendingpowers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModEntities.registerModEntities();





        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            // Only main hand, only empty hand, only with the tag
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!player.getMainHandStack().isEmpty()) return ActionResult.PASS;
            if (!player.getCommandTags().contains("earth_power")) return ActionResult.PASS;

            // Cancel on client, let server handle the logic
            if (world.isClient()) return ActionResult.SUCCESS;

            if (player.isSneaking()) {
                // --- Spawn ---
                boolean alreadyActive = !world.getEntitiesByType(
                        ModEntities.BOULDER,
                        player.getBoundingBox().expand(100),
                        b -> player.equals(b.getOwner())
                ).isEmpty();

                if (alreadyActive) return ActionResult.FAIL;

                BlockPos blockPos = hitResult.getBlockPos();
                Vec3d spawnPos = Vec3d.ofCenter(blockPos).add(0, 1.0, 0);

                BoulderEntity boulder = new BoulderEntity(ModEntities.BOULDER, world);
                boulder.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
                boulder.setOwner(player);
                world.spawnEntity(boulder);
            }

                return ActionResult.SUCCESS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                handleBoulderLaunch(player, world, hand));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                handleBoulderLaunch(player, world, hand));

	}

    private static ActionResult handleBoulderLaunch(PlayerEntity player, World world, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!player.getMainHandStack().isEmpty()) return ActionResult.PASS;
        if (!player.getCommandTags().contains("earth_power")) return ActionResult.PASS;

        // Cancel on client, handle server side
        if (world.isClient()) return ActionResult.SUCCESS;

        List<BoulderEntity> boulders = world.getEntitiesByType(
                ModEntities.BOULDER,
                player.getBoundingBox().expand(100),
                b -> player.equals(b.getOwner())
        );

        if (boulders.isEmpty()) return ActionResult.PASS;

        BoulderEntity boulder = boulders.get(0);
        if (boulder.getState() != 2) return ActionResult.PASS;

        boulder.setVelocity(player.getRotationVec(1.0f).multiply(2.5));
        boulder.velocityDirty = true;

        return ActionResult.SUCCESS;
    }
}