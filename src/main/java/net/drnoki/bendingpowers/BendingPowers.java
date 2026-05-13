package net.drnoki.bendingpowers;

import net.drnoki.bendingpowers.entity.ModEntities;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BendingPowers implements ModInitializer {

    // Very important comment
	public static final String MOD_ID = "bendingpowers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModEntities.registerModEntities();
	}
}