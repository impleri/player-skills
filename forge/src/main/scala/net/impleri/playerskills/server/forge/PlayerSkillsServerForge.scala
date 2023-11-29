package net.impleri.playerskills.server.forge

import net.impleri.playerskills.server.PlayerSkillsServer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

@EventBusSubscriber(value = Array(Dist.DEDICATED_SERVER))
object PlayerSkillsServerForge {
  @SubscribeEvent
  def onServerInit(event: FMLDedicatedServerSetupEvent): Unit = {
    PlayerSkillsServer.init()
  }
}
