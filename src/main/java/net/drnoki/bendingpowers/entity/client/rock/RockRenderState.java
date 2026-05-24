package net.drnoki.bendingpowers.entity.client.rock;

import net.drnoki.bendingpowers.entity.custom.RockEntity;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class RockRenderState extends EntityRenderState {

    /** Which phase the rock is in – copied from the entity each frame. */
    public RockEntity.State rockState = RockEntity.State.RISING;

    /** Entity ID – used for the golden-angle tilt so each rock looks unique. */
    public int entityId = 0;

    /** Fractional age (entity.age + tickProgress) for smooth spin interpolation. */
    public float age = 0f;
}
