package net.drnoki.bendingpowers.entity.client.earth_spike;

import net.drnoki.bendingpowers.BendingPowers;
import net.drnoki.bendingpowers.entity.custom.EarthSpikeEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;

public class EarthSpikeRenderer extends EntityRenderer<EarthSpikeEntity, EarthSpikeRenderState> {
    protected EarthSpikeModel model;
    public static final Identifier TEXTURE = Identifier.of(BendingPowers.MOD_ID, "textures/entity/earth_spike/earth_spike.png");
    public EarthSpikeRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new EarthSpikeModel(context.getPart(EarthSpikeModel.EARTH_SPIKE));
    }

    @Override
    public EarthSpikeRenderState createRenderState() {
        return new EarthSpikeRenderState();
    }

    @Override
    public void render(EarthSpikeRenderState state, MatrixStack matrixStack,
                       OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        matrixStack.push();
        matrixStack.scale(1.5f, 1.5f, 1.5f);

        List<RenderLayer> list = ItemRenderer.getGlintRenderLayers(this.model.getLayer(TEXTURE), false, false);

        for (int i = 0; i < list.size(); i++) {
            orderedRenderCommandQueue.getBatchingQueue(i)
                    .submitModel(
                            this.model,
                            state,
                            matrixStack,
                            (RenderLayer)list.get(i),
                            state.light,
                            OverlayTexture.DEFAULT_UV,
                            -1,
                            null,
                            state.outlineColor,
                            null
                    );
        }

        matrixStack.pop();

        super.render(state, matrixStack, orderedRenderCommandQueue, cameraRenderState);
    }

}
