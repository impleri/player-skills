package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.platform.Platform
import net.impleri.playerskills.restrictions.items.ItemRestrictionBuilder
import net.impleri.playerskills.restrictions.mobs.MobRestrictionBuilder
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.minecraft.server.MinecraftServer

internal class LifecycleEvents {
  private var serverInstance: MinecraftServer? = null

  internal val server: MinecraftServer by lazy {
    serverInstance ?: throw RuntimeException("Unable to access the server before it is available")
  }

  fun registerEvents() {
    LifecycleEvent.SETUP.register { onSetup() }
    LifecycleEvent.SERVER_BEFORE_START.register(LifecycleEvent.ServerState { beforeServerStart(it) })
    LifecycleEvent.SERVER_STARTED.register(LifecycleEvent.ServerState { onServerStart() })
    LifecycleEvent.SERVER_STOPPING.register(LifecycleEvent.ServerState { beforeSeverStops() })
  }

  private fun onSetup() {
    // Fill up the deferred skills registry
    Skills.resync()

    // TODO: Move to integration class
    // Enable FTB Teams integration if the mod is there
    if (Platform.isModLoaded("ftbteams")) {
      net.impleri.playerskills.integrations.ftbteams.FTBTeamsPlugin.registerInstance()
    }
  }

  private fun beforeServerStart(server: MinecraftServer) {
    serverInstance = server

    // Connect Player Skills Registry file storage
    SkillStorage.setup(server)
  }

  private fun onServerStart() {
    MobRestrictionBuilder.register()
    ItemRestrictionBuilder.register()
  }

  private fun beforeSeverStops() {
    Players.close()
    serverInstance = null
  }
}
