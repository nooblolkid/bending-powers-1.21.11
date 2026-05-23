package net.drnoki.bendingpowers.sound;

import com.sun.jna.platform.win32.Winspool;
import net.drnoki.bendingpowers.BendingPowers;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent EARTH_SPIKE_EMERSION = registerSoundEvent("earth_spike_emersion");


    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(BendingPowers.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        BendingPowers.LOGGER.info("Registering Mod Sounds for " + BendingPowers.MOD_ID);
    }
}
