package net.drnoki.bendingpowers.mixin;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.drnoki.bendingpowers.entity.custom.BoulderEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void onPlayerSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
        ServerPlayerEntity player = self.player;

        if (packet.getHand() != Hand.MAIN_HAND) return;
        if (!player.getMainHandStack().isEmpty()) return;
        if (!player.getCommandTags().contains("earth_power")) return;

        World world = player.getEntityWorld();

        List<BoulderEntity> boulders = world.getEntitiesByType(
                ModEntities.BOULDER,
                player.getBoundingBox().expand(100),
                b -> player.equals(b.getOwner())
        );

        if (boulders.isEmpty()) return;

        BoulderEntity boulder = boulders.get(0);
        if (boulder.getState() != 2) return;

        boulder.setVelocity(player.getRotationVec(1.0f).multiply(2.5));
        boulder.velocityDirty = true;
    }
}
