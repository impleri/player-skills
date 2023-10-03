package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.LifecycleEvent
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.minecraft.server.MinecraftServer

case class LifecycleEvents(onServerChange: Option[MinecraftServer] => Unit) {
  private[handlers] def registerEvents(): Unit = {
    LifecycleEvent.SETUP.register(() => onSetup())
    LifecycleEvent.SERVER_BEFORE_START.register(beforeServerStart(_))
    LifecycleEvent.SERVER_STARTED.register(_ => onServerStart())
    LifecycleEvent.SERVER_STOPPING.register(_ => beforeSeverStops())
  }

  private def onSetup(): Unit = {
    // Fill up the deferred skills registry
    Skills.resync()

    // TODO: Move to integration class
    // Enable FTB Teams integration if the mod is there
    //    if (Platform.isModLoaded("ftbteams")) {
    //      net.impleri.playerskills.integrations.ftbteams.FTBTeamsPlugin.registerInstance()
    //    }
  }

  private def beforeServerStart(server: MinecraftServer): Unit = {
    onServerChange(Some(server))
  }

  private def onServerStart(): Unit = {
    //    MobRestrictionBuilder.register()
    //    ItemRestrictionBuilder.register()
  }

  private def beforeSeverStops(): Unit = {
    Players.close()
    onServerChange(None)
  }
}
