package net.impleri.playerskills.forge;

import dev.architectury.platform.forge.EventBuses;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.client.PlayerSkillsClient;
import net.impleri.playerskills.server.PlayerSkillsServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PlayerSkills.MOD_ID)
public class PlayerSkillsForge {
    public PlayerSkillsForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(PlayerSkills.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PlayerSkills.init();
    }

    @SubscribeEvent
    public static void onClientInit(FMLClientSetupEvent event) {
        PlayerSkillsClient.init();
    }

    @SubscribeEvent
    public static void onServerInit(FMLDedicatedServerSetupEvent event) {
        PlayerSkillsServer.init();
    }
}
