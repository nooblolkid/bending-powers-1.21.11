package net.drnoki.bendingpowers.entity.client.boulder;

import net.drnoki.bendingpowers.BendingPowers;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class BoulderModel extends EntityModel<BoulderRenderState> {
    public static final EntityModelLayer BOULDER = new EntityModelLayer(Identifier.of(BendingPowers.MOD_ID, "boulder"), "main");
    private final ModelPart boulder;
    private final ModelPart small_rock;
    private final ModelPart small_rock3;
    private final ModelPart small_rock4;
    private final ModelPart small_rock5;
    private final ModelPart small_rock2;
    private final ModelPart small_rock6;
    private final ModelPart small_rock7;
    private final ModelPart rock;
    private final ModelPart rock3;
    private final ModelPart rock2;
    private final ModelPart rock4;
    private final ModelPart large_rock;
    private final ModelPart large_rock2;

    private final Animation idleAnimation;
    private final Animation formationAnimation;

    public BoulderModel(ModelPart root) {
        super(root);
        this.boulder = root.getChild("boulder");
        this.small_rock = this.boulder.getChild("small_rock");
        this.small_rock3 = this.boulder.getChild("small_rock3");
        this.small_rock4 = this.boulder.getChild("small_rock4");
        this.small_rock5 = this.boulder.getChild("small_rock5");
        this.small_rock2 = this.boulder.getChild("small_rock2");
        this.small_rock6 = this.boulder.getChild("small_rock6");
        this.small_rock7 = this.boulder.getChild("small_rock7");
        this.rock = this.boulder.getChild("rock");
        this.rock3 = this.boulder.getChild("rock3");
        this.rock2 = this.boulder.getChild("rock2");
        this.rock4 = this.boulder.getChild("rock4");
        this.large_rock = this.boulder.getChild("large_rock");
        this.large_rock2 = this.boulder.getChild("large_rock2");

        this.idleAnimation = BoulderAnimations.IDLE.createAnimation(root);
        this.formationAnimation = BoulderAnimations.FORMATION.createAnimation(root);
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData boulder = modelPartData.addChild("boulder", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData small_rock = boulder.addChild("small_rock", ModelPartBuilder.create().uv(40, 64).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(9.4615F, 0.9231F, -3.1539F, -0.3491F, 0.0F, 0.0F));

        ModelPartData cube2_r1 = small_rock.addChild("cube2_r1", ModelPartBuilder.create().uv(52, 64).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock3 = boulder.addChild("small_rock3", ModelPartBuilder.create().uv(64, 56).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(13.4615F, 0.9231F, 1.8461F, -0.3491F, 0.0F, 0.0F));

        ModelPartData cube4_r1 = small_rock3.addChild("cube4_r1", ModelPartBuilder.create().uv(64, 62).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock4 = boulder.addChild("small_rock4", ModelPartBuilder.create().uv(72, 0).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(13.4615F, 0.9231F, -8.1538F, -0.3491F, 0.0F, 0.0F));

        ModelPartData cube6_r1 = small_rock4.addChild("cube6_r1", ModelPartBuilder.create().uv(72, 6).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock5 = boulder.addChild("small_rock5", ModelPartBuilder.create().uv(64, 68).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(16.4615F, 0.9231F, -3.1539F, -0.3491F, 0.0F, 0.0F));

        ModelPartData cube8_r1 = small_rock5.addChild("cube8_r1", ModelPartBuilder.create().uv(16, 70).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock2 = boulder.addChild("small_rock2", ModelPartBuilder.create().uv(16, 64).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(6.4615F, 0.9231F, 6.8461F, -0.836F, 0.1336F, -1.1215F));

        ModelPartData cube10_r1 = small_rock2.addChild("cube10_r1", ModelPartBuilder.create().uv(28, 64).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock6 = boulder.addChild("small_rock6", ModelPartBuilder.create().uv(28, 70).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(11.4615F, 0.9231F, 11.8462F, -0.836F, 0.1336F, -1.1215F));

        ModelPartData cube12_r1 = small_rock6.addChild("cube12_r1", ModelPartBuilder.create().uv(40, 70).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData small_rock7 = boulder.addChild("small_rock7", ModelPartBuilder.create().uv(52, 70).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(1.4615F, 0.9231F, 11.8462F, -0.836F, 0.1336F, -1.1215F));

        ModelPartData cube14_r1 = small_rock7.addChild("cube14_r1", ModelPartBuilder.create().uv(0, 72).cuboid(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.25F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -0.7854F));

        ModelPartData rock = boulder.addChild("rock", ModelPartBuilder.create().uv(56, 16).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.origin(-16.0385F, -0.5769F, 0.3461F));

        ModelPartData cube17_r1 = rock.addChild("cube17_r1", ModelPartBuilder.create().uv(56, 24).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.653F, 1.0416F, 2.4016F));

        ModelPartData cube15_r1 = rock.addChild("cube15_r1", ModelPartBuilder.create().uv(16, 56).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0291F, -0.5473F, 0.7691F));

        ModelPartData rock3 = boulder.addChild("rock3", ModelPartBuilder.create().uv(56, 32).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.origin(-23.0385F, -0.5769F, -3.6539F));

        ModelPartData cube20_r1 = rock3.addChild("cube20_r1", ModelPartBuilder.create().uv(56, 40).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.653F, 1.0416F, 2.4016F));

        ModelPartData cube18_r1 = rock3.addChild("cube18_r1", ModelPartBuilder.create().uv(32, 56).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0291F, -0.5473F, 0.7691F));

        ModelPartData rock2 = boulder.addChild("rock2", ModelPartBuilder.create().uv(0, 56).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.origin(-7.0385F, -0.5769F, 8.3462F));

        ModelPartData cube22_r1 = rock2.addChild("cube22_r1", ModelPartBuilder.create().uv(56, 8).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.1337F, -0.5331F, 0.5661F));

        ModelPartData cube21_r1 = rock2.addChild("cube21_r1", ModelPartBuilder.create().uv(56, 0).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.4041F, 0.7001F, 0.2689F));

        ModelPartData rock4 = boulder.addChild("rock4", ModelPartBuilder.create().uv(0, 64).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.origin(-16.0385F, -0.5769F, 10.3462F));

        ModelPartData cube25_r1 = rock4.addChild("cube25_r1", ModelPartBuilder.create().uv(56, 48).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.1337F, -0.5331F, 0.5661F));

        ModelPartData cube24_r1 = rock4.addChild("cube24_r1", ModelPartBuilder.create().uv(48, 56).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.5F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.4041F, 0.7001F, 0.2689F));

        ModelPartData large_rock = boulder.addChild("large_rock", ModelPartBuilder.create().uv(0, 0).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.origin(4.4615F, -2.0769F, -17.1538F));

        ModelPartData cube30_r1 = large_rock.addChild("cube30_r1", ModelPartBuilder.create().uv(28, 0).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.6059F, 0.2816F, -0.1772F));

        ModelPartData cube29_r1 = large_rock.addChild("cube29_r1", ModelPartBuilder.create().uv(0, 14).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.4363F, -0.3054F, -0.2182F));

        ModelPartData cube28_r1 = large_rock.addChild("cube28_r1", ModelPartBuilder.create().uv(0, 28).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3828F, -0.7315F, -0.8152F));

        ModelPartData large_rock2 = boulder.addChild("large_rock2", ModelPartBuilder.create().uv(28, 14).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.75F)), ModelTransform.origin(-14.5385F, -2.0769F, -16.1538F));

        ModelPartData cube34_r1 = large_rock2.addChild("cube34_r1", ModelPartBuilder.create().uv(28, 42).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.75F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.4679F, 0.4849F, -0.5407F));

        ModelPartData cube33_r1 = large_rock2.addChild("cube33_r1", ModelPartBuilder.create().uv(0, 42).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.75F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.4878F, -0.2078F, -0.42F));

        ModelPartData cube32_r1 = large_rock2.addChild("cube32_r1", ModelPartBuilder.create().uv(28, 28).cuboid(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new Dilation(0.75F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.2519F, -0.7315F, -0.8152F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(BoulderRenderState state) {
        super.setAngles(state);
        this.root.traverse().forEach(ModelPart::resetTransform);


        this.formationAnimation.apply(state.formationAnimationState, state.age);
        this.idleAnimation.apply(state.idleAnimationState, state.age);
    }
}
