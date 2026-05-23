package net.drnoki.bendingpowers.entity.client.earth_spike;

import net.minecraft.client.render.entity.animation.AnimationDefinition;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class EarthSpikeAnimations {
    public static final AnimationDefinition eruption = AnimationDefinition.Builder.create(1.0F)
            .addBoneAnimation("first", new Transformation(Transformation.Targets.SCALE,
                    new Keyframe(0.0F, AnimationHelper.createScalingVector(1.0F, 1.0F, 1.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("second", new Transformation(Transformation.Targets.MOVE_ORIGIN,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -25.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("third", new Transformation(Transformation.Targets.MOVE_ORIGIN,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -48.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("fourth", new Transformation(Transformation.Targets.MOVE_ORIGIN,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -66.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("fifth", new Transformation(Transformation.Targets.MOVE_ORIGIN,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, -92.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .build();
}
