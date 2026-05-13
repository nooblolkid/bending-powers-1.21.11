package net.drnoki.bendingpowers;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.drnoki.bendingpowers.entity.client.boulder.BoulderModel;
import net.drnoki.bendingpowers.entity.client.boulder.BoulderRender;
import net.drnoki.bendingpowers.entity.client.earth_spike.EarthSpikeModel;
import net.drnoki.bendingpowers.entity.client.earth_spike.EarthSpikeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BendingPowersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityModelLayerRegistry.registerModelLayer(BoulderModel.BOULDER, BoulderModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.BOULDER, BoulderRender::new);
        EntityModelLayerRegistry.registerModelLayer(EarthSpikeModel.EARTH_SPIKE, EarthSpikeModel::getTexturedModelData);
        EntityRendererRegistry.register(ModEntities.EARTH_SPIKE, EarthSpikeRenderer::new);
    }
}
