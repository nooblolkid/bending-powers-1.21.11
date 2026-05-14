package net.drnoki.bendingpowers.entity;

import net.drnoki.bendingpowers.BendingPowers;
import net.drnoki.bendingpowers.entity.custom.BoulderEntity;
import net.drnoki.bendingpowers.entity.custom.EarthSpikeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;


public class ModEntities {
    private static final RegistryKey<EntityType<?>> BOULDER_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(BendingPowers.MOD_ID, "boulder"));
    private static final RegistryKey<EntityType<?>> EARTH_SPIKE_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(BendingPowers.MOD_ID, "earth_spike"));

    public static final EntityType<BoulderEntity> BOULDER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(BendingPowers.MOD_ID, "boulder"),
            EntityType.Builder.create(BoulderEntity::new, SpawnGroup.MISC)
                    .dimensions(1.5f, 1.5f).build(BOULDER_KEY));
    public static final EntityType<EarthSpikeEntity> EARTH_SPIKE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(BendingPowers.MOD_ID, "earth_spike"),
            EntityType.Builder.create(EarthSpikeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.4f, 1.8f).build(EARTH_SPIKE_KEY));

    public static void registerModEntities() {
        BendingPowers.LOGGER.info("Registering Mod Entities for " + BendingPowers.MOD_ID);
    }
}
