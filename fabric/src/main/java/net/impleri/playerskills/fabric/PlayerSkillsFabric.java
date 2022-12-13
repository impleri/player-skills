package net.impleri.playerskills.fabric;

import net.fabricmc.api.ModInitializer;
import net.impleri.playerskills.PlayerSkills;

public class PlayerSkillsFabric implements ModInitializer {
    public void onInitialize() {
        PlayerSkills.init();
    }
}
