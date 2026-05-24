package net.drnoki.bendingpowers.entity.client.rock;

import net.drnoki.bendingpowers.BendingPowers;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class RockModel extends EntityModel<RockRenderState> {
    public static final EntityModelLayer ROCK = new EntityModelLayer(Identifier.of(BendingPowers.MOD_ID, "rock"), "main");

    private final ModelPart bb_main;

    public RockModel(ModelPart root) {
        super(root);
        this.bb_main = root.getChild("bb_main");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.5F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 24.0F, 0.0F));

        ModelPartData cube_r1 = bb_main.addChild("cube_r1", ModelPartBuilder.create().uv(-2, -2).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -1.0F, 1.0F, 0.0F, 1.5708F, 0.2618F));

        ModelPartData cube_r2 = bb_main.addChild("cube_r2", ModelPartBuilder.create().uv(0, 15).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(0.3F, -0.8F, -0.4F, 0.0F, 0.2618F, -0.2618F));

        ModelPartData cube_r3 = bb_main.addChild("cube_r3", ModelPartBuilder.create().uv(0, 8).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, -1.3F, -0.1F, 0.0F, 0.0F, 0.2618F));
        return TexturedModelData.of(modelData, 32, 32);
    }
}
