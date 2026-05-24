package net.drnoki.bendingpowers.entity.client.rock;

import net.drnoki.bendingpowers.BendingPowers;
import net.drnoki.bendingpowers.entity.custom.RockEntity;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

public class RockRenderer extends EntityRenderer<RockEntity, RockRenderState> {

    public static final Identifier TEXTURE =
            Identifier.of(BendingPowers.MOD_ID, "textures/entity/rock/rock.png");

    protected final RockModel model;

    public RockRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new RockModel(context.getPart(RockModel.ROCK));
    }

    // ── RenderState lifecycle ─────────────────────────────────────────────

    @Override
    public RockRenderState createRenderState() {
        return new RockRenderState();
    }

    /**
     * Called on the GAME thread – copy whatever you need from the live entity
     * into the RenderState before the render thread reads it.
     * Never access the entity inside render() itself.
     */
    @Override
    public void updateRenderState(RockEntity entity, RockRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.rockState = entity.getState();
        state.entityId  = entity.getId();
        state.age       = entity.age + tickProgress; // smooth interpolated age
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    @Override
    public void render(
            RockRenderState          state,
            MatrixStack               matrixStack,
            OrderedRenderCommandQueue queue,
            CameraRenderState         cameraRenderState
    ) {
        matrixStack.push();

        // Scale: grows from small as it rises, settled at idle, slightly larger flying
        float scale = switch (state.rockState) {
            case RISING   -> Math.min(0.45f, 0.15f + state.age * 0.015f);
            case IDLE     -> 0.45f;
            case LAUNCHED -> 0.55f;
        };
        matrixStack.scale(scale, scale, scale);

        // Rotation
        if (state.rockState == RockEntity.State.IDLE) {
            // No spin at idle – fixed unique tilt per rock via golden angle
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20f));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.entityId * 137.5f));
        } else {
            float spinRate = state.rockState == RockEntity.State.LAUNCHED ? 14f : 5f;
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.age * spinRate));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.age * spinRate * 0.6f));
        }

        // Submit model using the same pattern as your BoulderRender
        List<RenderLayer> layers = ItemRenderer.getGlintRenderLayers(
                this.model.getLayer(TEXTURE), false, false
        );

        for (int i = 0; i < layers.size(); i++) {
            queue.getBatchingQueue(i).submitModel(
                    this.model,
                    state,
                    matrixStack,
                    layers.get(i),
                    state.light,
                    OverlayTexture.DEFAULT_UV,
                    -1,
                    null,
                    state.outlineColor,
                    null
            );
        }

        matrixStack.pop();
        super.render(state, matrixStack, queue, cameraRenderState);
    }
}
