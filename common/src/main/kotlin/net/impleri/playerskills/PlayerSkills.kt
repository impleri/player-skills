package net.impleri.playerskills

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.platform.Platform
import dev.architectury.registry.ReloadListenerRegistry
import dev.architectury.registry.registries.DeferredRegister
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.commands.PlayerSkillsCommands
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.SkillTypes
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.skills.registry.storage.SkillStorage
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.entity.player.Player
import java.util.UUID
import java.util.function.Consumer

class PlayerSkills : ResourceManagerReloadListener {
  private var serverInstance: MinecraftServer? = null

  fun registerEvents() {
    LifecycleEvent.SETUP.register(Runnable { onSetup() })
    LifecycleEvent.SERVER_BEFORE_START.register(LifecycleEvent.ServerState { beforeServerStart(it) })
    LifecycleEvent.SERVER_STOPPING.register(LifecycleEvent.ServerState { beforeSeverStops() })
    PlayerEvent.PLAYER_JOIN.register(PlayerEvent.PlayerJoin { onPlayerJoin(it) })
    PlayerEvent.PLAYER_QUIT.register(PlayerEvent.PlayerQuit { onPlayerQuit(it) })

    ReloadListenerRegistry.register(PackType.SERVER_DATA, this)

    SkillChangedEvent.EVENT.register(Consumer { NetHandler.syncPlayer(it) })
    CommandRegistrationEvent.EVENT.register(
      CommandRegistrationEvent { dispatcher: CommandDispatcher<CommandSourceStack>, _: CommandBuildContext, _: Commands.CommandSelection ->
        PlayerSkillsCommands.register(dispatcher)
      },
    )
  }

  fun <T> resyncPlayer(playerId: UUID) {
    serverInstance?.playerList?.getPlayer(playerId)?.let { NetHandler.syncPlayer(it) }
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

  private fun onPlayerJoin(player: ServerPlayer) {
    Players.open(player.uuid)
    NetHandler.syncPlayer(player, true)
  }

  private fun onPlayerQuit(player: ServerPlayer) {
    Players.close(player.uuid)
  }

  private fun beforeSeverStops() {
    Players.close()
    serverInstance = null
  }

  companion object {
    const val MOD_ID = "playerskills"

    val LOGGER = PlayerSkillsLogger.create(MOD_ID, "SKILLS")

    private val SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey<SkillType<*>>(SkillTypes.REGISTRY_KEY)
    private val SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY)
    private val INSTANCE = PlayerSkills()

    fun init() {
      registerTypes()

      INSTANCE.registerEvents()

      Manager.register()

      LOGGER.info("PlayerSkills Loaded")

      // @TODO: Maybe move elsewhere?
      if (Platform.isModLoaded("ftbquests")) {
        net.impleri.playerskills.integrations.ftbquests.PlayerSkillsIntegration.init()
      }
    }

    private fun registerTypes() {
      SkillTypes.buildRegistry()
      Skills.buildRegistry()
      SKILL_TYPES.register(BasicSkillType.NAME) { BasicSkillType() }
      SKILL_TYPES.register(NumericSkillType.NAME) { NumericSkillType() }
      SKILL_TYPES.register(TieredSkillType.NAME) { TieredSkillType() }
      SKILL_TYPES.register(SpecializedSkillType.NAME) { SpecializedSkillType() }
      SKILL_TYPES.register()
    }

    fun toggleDebug(): Boolean {
      return LOGGER.toggleDebug()
    }

    fun <T> emitSkillChanged(player: Player, newSkill: Skill<T>, oldSkill: Skill<T>) {
      SkillChangedEvent.EVENT.invoker().accept(SkillChangedEvent(player, newSkill, oldSkill))

      if (player is ServerPlayer) {
        val message = newSkill.getNotification(oldSkill.value)
        if (message != null) {
          player.sendSystemMessage(message, true)
        }
      }
    }

    fun <T> resync(playerId: UUID) {
      INSTANCE.resyncPlayer<T>(playerId)
    }
  }
}
