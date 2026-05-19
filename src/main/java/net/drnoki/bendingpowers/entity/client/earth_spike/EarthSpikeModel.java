package net.drnoki.bendingpowers.entity.client.earth_spike;

import net.drnoki.bendingpowers.BendingPowers;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class EarthSpikeModel extends EntityModel<EarthSpikeRenderState> {
    public static final EntityModelLayer EARTH_SPIKE = new EntityModelLayer(Identifier.of(BendingPowers.MOD_ID, "earth_spike"), "main");
    private final ModelPart root;
    private final ModelPart first;
    private final ModelPart second;
    private final ModelPart third;
    private final ModelPart fourth;
    private final ModelPart fifth;
    public EarthSpikeModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.first = this.root.getChild("first");
        this.second = this.root.getChild("second");
        this.third = this.root.getChild("third");
        this.fourth = this.root.getChild("fourth");
        this.fifth = this.root.getChild("fifth");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(), ModelTransform.of(0.0F, -6.0F, 0.0F, -1.5708F, -1.1781F, -1.5708F));

        ModelPartData first = root.addChild("first", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -9.0F, -8.0F, 16.0F, 25.0F, 16.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData second = root.addChild("second", ModelPartBuilder.create().uv(0, 41).cuboid(-8.0F, -29.0F, -8.0F, 14.0F, 23.0F, 14.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -3.0F, 0.0F));

        ModelPartData third = root.addChild("third", ModelPartBuilder.create().uv(56, 41).cuboid(-8.0F, -44.0F, -6.0F, 12.0F, 18.0F, 12.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -6.0F, 0.0F));

        ModelPartData fourth = root.addChild("fourth", ModelPartBuilder.create().uv(56, 71).cuboid(-7.0F, -61.0F, -6.0F, 10.0F, 20.0F, 10.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -9.0F, 0.0F));

        ModelPartData fifth = root.addChild("fifth", ModelPartBuilder.create().uv(64, 0).cuboid(-7.0F, -92.0F, -5.0F, 8.0F, 31.0F, 8.0F, new Dilation(0.0F)), ModelTransform.origin(2.0F, -9.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }
}
