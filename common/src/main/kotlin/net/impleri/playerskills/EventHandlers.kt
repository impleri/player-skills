package net.impleri.playerskills

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.platform.Platform
import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.api.ItemRestrictionBuilder
import net.impleri.playerskills.api.MobRestrictionBuilder
import net.impleri.playerskills.commands.PlayerSkillsCommands
import net.impleri.playerskills.data.ItemRestrictionDataLoader
import net.impleri.playerskills.data.MobRestrictionDataLoader
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.items.ItemEventHandlers
import net.impleri.playerskills.mobs.MobEventHandlers
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import java.util.UUID
import java.util.function.Consumer

internal class EventHandlers : ResourceManagerReloadListener {
  private var serverInstance: MinecraftServer? = null

  internal val server: MinecraftServer by lazy {
    serverInstance ?: throw RuntimeException("Unable to access the server before it is available")
  }

  fun registerEvents() {
    // Architectury Events
    LifecycleEvent.SETUP.register { onSetup() }
    LifecycleEvent.SERVER_BEFORE_START.register(LifecycleEvent.ServerState { beforeServerStart(it) })
    LifecycleEvent.SERVER_STARTED.register(LifecycleEvent.ServerState { onServerStart() })
    LifecycleEvent.SERVER_STOPPING.register(LifecycleEvent.ServerState { beforeSeverStops() })

    PlayerEvent.PLAYER_JOIN.register(PlayerEvent.PlayerJoin { onPlayerJoin(it) })
    PlayerEvent.PLAYER_QUIT.register(PlayerEvent.PlayerQuit { onPlayerQuit(it) })

    CommandRegistrationEvent.EVENT.register(
      CommandRegistrationEvent { dispatcher: CommandDispatcher<CommandSourceStack>, _: CommandBuildContext, _: Commands.CommandSelection ->
        PlayerSkillsCommands.register(dispatcher)
      },
    )

    // Player Skills Events
    SkillChangedEvent.EVENT.register(Consumer { NetHandler.syncPlayer(it) })

    // Vanilla Events
    ReloadListenerRegistry.register(PackType.SERVER_DATA, this)
    ReloadListenerRegistry.register(PackType.SERVER_DATA, SkillsDataLoader())
    ReloadListenerRegistry.register(PackType.SERVER_DATA, ItemRestrictionDataLoader())
    ReloadListenerRegistry.register(PackType.SERVER_DATA, MobRestrictionDataLoader())
  }

  override fun onResourceManagerReload(resourceManager: ResourceManager) {
    val players = Players.close()
    Players.open(players)

    serverInstance?.playerList
      ?.players
      ?.forEach { NetHandler.syncPlayer(it) }
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

  private fun onPlayerJoin(player: ServerPlayer) {
    Players.open(player.uuid)
    NetHandler.syncPlayer(player, true)
  }

  private fun onPlayerQuit(player: ServerPlayer) {
    Players.close(player.uuid)
  }

  fun resyncPlayer(playerId: UUID) {
    serverInstance?.playerList?.getPlayer(playerId)?.let { NetHandler.syncPlayer(it) }
  }

  companion object {
    private val INSTANCE = EventHandlers()

    internal val server: Lazy<MinecraftServer> = lazy { INSTANCE.server }

    fun init() {
      INSTANCE.registerEvents()
      ItemEventHandlers.init()
      MobEventHandlers.init()
    }

    fun resync(playerId: UUID) {
      INSTANCE.resyncPlayer(playerId)
    }
  }
}
