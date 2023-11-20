package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.LifecycleEvent
import net.impleri.playerskills.facades.minecraft.{Server => ServerFacade}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.minecraft.server.MinecraftServer

case class LifecycleEvents(onServerChange: Option[ServerFacade] => Unit = _ => {}) {
  private[server] def registerEvents(): Unit = {
    LifecycleEvent.SERVER_BEFORE_START.register(beforeServerStart(_))
    LifecycleEvent.SERVER_STARTED.register(_ => onServerStart())
    LifecycleEvent.SERVER_STOPPING.register(_ => beforeSeverStops())
  }
  
  private def beforeServerStart(server: MinecraftServer): Unit = {
    onServerChange(Option(server).map(ServerFacade.apply))
  }

  private def onServerStart(): Unit = {
    //    MobRestrictionBuilder.register()
    //    ItemRestrictionBuilder.register()
  }

  private def beforeSeverStops(): Unit = {
    PlayerSkillsServer.STATE.PLAYERS.close()
    onServerChange(None)
  }
}
