package net.impleri.playerskills.client.forge

import net.impleri.playerskills.client.PlayerSkillsClient
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

@EventBusSubscriber(value = [Dist.CLIENT])
object PlayerSkillsClientForge {
  @SubscribeEvent
  fun onClientInit(event: FMLClientSetupEvent?) {
    PlayerSkillsClient.init()
  }
}
