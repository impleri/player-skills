package net.impleri.playerskills.forge;

import dev.architectury.platform.forge.EventBuses;
import net.impleri.playerskills.PlayerSkillsCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PlayerSkillsCore.MOD_ID)
public class PlayerSkillsCoreForge {
    public PlayerSkillsCoreForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(PlayerSkillsCore.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PlayerSkillsCore.init();
    }
}
