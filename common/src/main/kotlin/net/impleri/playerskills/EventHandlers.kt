package net.impleri.playerskills

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.platform.Platform
import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.api.MobRestrictionBuilder
import net.impleri.playerskills.api.MobRestrictions
import net.impleri.playerskills.commands.PlayerSkillsCommands
import net.impleri.playerskills.data.MobRestrictionDataLoader
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.mobs.MobSkills
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.Vec3i
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.LevelAccessor
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
    LifecycleEvent.SERVER_STARTING.register(LifecycleEvent.ServerState { onServerStart() })
    LifecycleEvent.SERVER_STOPPING.register(LifecycleEvent.ServerState { beforeSeverStops() })

    PlayerEvent.PLAYER_JOIN.register(PlayerEvent.PlayerJoin { onPlayerJoin(it) })
    PlayerEvent.PLAYER_QUIT.register(PlayerEvent.PlayerQuit { onPlayerQuit(it) })

    EntityEvent.LIVING_CHECK_SPAWN.register(
      EntityEvent.LivingCheckSpawn { livingEntity: LivingEntity, levelAccessor: LevelAccessor, x: Double, y: Double, z: Double, mobSpawnType: MobSpawnType, _: BaseSpawner? ->
        onCheckSpawn(
          livingEntity,
          levelAccessor,
          x,
          y,
          z,
          mobSpawnType,
        )
      },
    )

    InteractionEvent.INTERACT_ENTITY.register(
      InteractionEvent.InteractEntity { player: Player, entity: Entity, _: InteractionHand ->
        onInteract(
          player,
          entity,
        )
      },
    )

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

  private fun onInteract(player: Player, entity: Entity): EventResult {
    if (MobRestrictions.canInteractWith(entity.type, player)) {
      return EventResult.pass()
    }
    MobSkills.LOGGER.debug("Preventing ${player.name} from interacting with ${MobRestrictions.getName(entity.type)}")
    return EventResult.interruptFalse()
  }

  private fun onCheckSpawn(
    livingEntity: LivingEntity,
    levelAccessor: LevelAccessor,
    x: Double,
    y: Double,
    z: Double,
    mobSpawnType: MobSpawnType,
  ): EventResult {
    val pos = Vec3i(x, y, z)
    if (MobRestrictions.canSpawn(livingEntity, levelAccessor, pos, mobSpawnType)) {
      return EventResult.pass()
    }
    MobSkills.LOGGER.debug("Preventing ${MobRestrictions.getName(livingEntity.type)} from spawning at ${pos.toShortString()}")
    return EventResult.interruptFalse()
  }

  fun resyncPlayer(playerId: UUID) {
    serverInstance?.playerList?.getPlayer(playerId)?.let { NetHandler.syncPlayer(it) }
  }

  companion object {
    private val INSTANCE = EventHandlers()

    internal val server: Lazy<MinecraftServer> = lazy { INSTANCE.server }

    fun init() {
      INSTANCE.registerEvents()
    }

    fun resync(playerId: UUID) {
      INSTANCE.resyncPlayer(playerId)
    }
  }
}
