package net.impleri.playerskills.fabric;

import net.fabricmc.api.ModInitializer;
import net.impleri.playerskills.PlayerSkillsCore;

public class PlayerSkillsFabric implements ModInitializer {
    public void onInitialize() {
        PlayerSkillsCore.init();
    }
}
