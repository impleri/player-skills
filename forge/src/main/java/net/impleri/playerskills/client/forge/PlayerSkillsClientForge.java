package net.impleri.playerskills.client.forge;

import net.impleri.playerskills.client.PlayerSkillsClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerSkillsClientForge {
    @SubscribeEvent
    public static void onClientInit(FMLClientSetupEvent event) {
        PlayerSkillsClient.init();
    }
}
