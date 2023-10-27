package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.LifecycleEvent
import net.impleri.playerskills.server.ServerStateContainer
import net.minecraft.server.MinecraftServer

case class LifecycleEvents(onSetup: () => Unit, onServerChange: Option[MinecraftServer] => Unit) {
  private[handlers] def registerEvents(): Unit = {
    LifecycleEvent.SETUP.register(() => setup())
    LifecycleEvent.SERVER_BEFORE_START.register(beforeServerStart(_))
    LifecycleEvent.SERVER_STARTED.register(_ => onServerStart())
    LifecycleEvent.SERVER_STOPPING.register(_ => beforeSeverStops())
  }

  private def setup(): Unit = {
    onSetup()
  }

  private def beforeServerStart(server: MinecraftServer): Unit = {
    onServerChange(Option(server))
  }

  private def onServerStart(): Unit = {
    //    MobRestrictionBuilder.register()
    //    ItemRestrictionBuilder.register()
  }

  private def beforeSeverStops(): Unit = {
    ServerStateContainer.PLAYERS.close()
    onServerChange(None)
  }
}
