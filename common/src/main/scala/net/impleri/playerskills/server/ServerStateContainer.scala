package net.impleri.playerskills.server

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.integrations.IntegrationLoader
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.restrictions.item.ItemRestrictionBuilder
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.server.bindings.BlockEvents
import net.impleri.playerskills.server.bindings.CommandEvents
import net.impleri.playerskills.server.bindings.EntityEvents
import net.impleri.playerskills.server.bindings.InternalEvents
import net.impleri.playerskills.server.bindings.LifecycleEvents
import net.impleri.playerskills.server.bindings.PlayerEvents
import net.impleri.playerskills.server.bindings.TickEvents
import net.impleri.playerskills.server.commands.PlayerSkillsCommands
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.item.Item

import scala.annotation.unused

/**
 * Single place for all stateful classes
 */
case class ServerStateContainer(
  private val globalState: StateContainer = StateContainer(),
  var PLAYERS: PlayerRegistry = PlayerRegistry(),
  private val eventHandler: EventHandler = EventHandler(),
  private val reloadListeners: ReloadListeners = ReloadListeners(true),
  var TEAM: Team = StubTeam(),
  var SERVER: Option[Server] = None,
  private val itemRegistry: Registry[Item] = Registry.Items,
  private val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  private var STORAGE: Option[PlayerStorageIO] = SERVER.map(
    PlayerStorageIO(_, skillTypeOps = globalState.SKILL_TYPE_OPS),
  )

  var PLAYER_OPS: Player = Player(PLAYERS, globalState.SKILL_TYPE_OPS, globalState.SKILL_OPS)
  var TEAM_OPS: TeamOps = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)

  lazy private val MANAGER = Manager(globalState, serverStateContainer = Option(this))

  private val LIFECYCLE = LifecycleEvents(PLAYERS, onSetup, onServerChange)
  private val INTERNAL = InternalEvents(ItemRestrictionBuilder(itemRegistry, globalState.RESTRICTIONS),
    eventHandler,
    globalState,
    this,
    onReload,
    reloadListeners,
  )
  private val COMMAND = CommandEvents()
  private val PLAYER = PlayerEvents(PLAYERS, getNetHandler)
  private val TICK = TickEvents(globalState.ITEM_RESTRICTIONS)
  private val ENTITY = EntityEvents(globalState.ITEM_RESTRICTIONS)
  private val BLOCK = BlockEvents(globalState.ITEM_RESTRICTIONS)

  private val INTEGRATIONS = IntegrationLoader(this)

  LIFECYCLE.registerEvents()
  INTERNAL.registerEvents()
  COMMAND.registerEvents(
    PlayerSkillsCommands(
      globalState.SKILL_OPS,
      globalState.SKILL_TYPE_OPS,
      PLAYER_OPS,
      TEAM_OPS,
    ),
  )
  PLAYER.registerEvents()
  TICK.registerEventHandlers()
  ENTITY.registerEvents()
  BLOCK.registerEvents()

  logger.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = {
    TEAM = instance
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)
  }

  private def onSetup(): Unit = {
    INTEGRATIONS.onSetup()
  }

  private[server] def onServerChange(next: Option[Server] = None): Unit = {
    SERVER = next
    STORAGE = SERVER.map(PlayerStorageIO(_, skillTypeOps = globalState.SKILL_TYPE_OPS))
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState, globalState.SKILLS)
    PLAYER_OPS = Player(PLAYERS, globalState.SKILL_TYPE_OPS, globalState.SKILL_OPS)
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)
    //    NET_HANDLER = NetHandler(PLAYER_OPS, MANAGER.SYNC_SKILLS)
  }

  private[server] def onReload(@unused resourceManager: ResourceManager): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers).foreach {
      val netHandler = getNetHandler
      _.foreach(netHandler.syncPlayer(_))
    }
  }

  def getNetHandler: NetHandler = NetHandler(PLAYER_OPS, MANAGER.SYNC_SKILLS)
}
