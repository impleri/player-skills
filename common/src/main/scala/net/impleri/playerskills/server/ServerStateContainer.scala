package net.impleri.playerskills.server

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.restrictions.item.ItemRestrictionBuilder
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionBuilder
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.server.bindings.InternalEvents
import net.impleri.playerskills.server.bindings.ServerEventBindings
import net.impleri.playerskills.server.commands.PlayerSkillsCommands
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ReloadListeners
import net.impleri.slab.resources.ResourceManager
import net.impleri.slab.server.Server

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
  private val itemRegistry: Registry.ITEM = Registry.Items,
  private val logger: Logger = PlayerSkillsLogger.SKILLS,
) {
  private var STORAGE: Option[PlayerStorageIO] = SERVER.map(
    PlayerStorageIO(_, skillTypeOps = globalState.SKILL_TYPE_OPS),
  )

  var PLAYER_OPS: Player = Player(PLAYERS, globalState.SKILL_TYPE_OPS, globalState.SKILL_OPS)
  var TEAM_OPS: TeamOps = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)

  lazy private val MANAGER = Manager(globalState, serverStateContainer = Option(this))

  private val EVENT_BINDINGS = ServerEventBindings(
    PLAYERS,
    globalState,
    this,
    onServerChange,
    () => PlayerSkillsCommands(
      globalState.SKILL_OPS,
      globalState.SKILL_TYPE_OPS,
      PLAYER_OPS,
      TEAM_OPS,
    ),
    getNetHandler,
  )

  private val INTERNAL = InternalEvents(
    ItemRestrictionBuilder(Option(itemRegistry), globalState.RESTRICTIONS),
    RecipeRestrictionBuilder(this, globalState.RESTRICTIONS),
    eventHandler,
    globalState,
    this,
    onReload,
    reloadListeners,
  )

  EVENT_BINDINGS.registerEvents()
  INTERNAL.registerEvents()

  logger.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = {
    TEAM = instance
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)
  }

  private[server] def onServerChange(next: Option[Server] = None): Unit = {
    SERVER = next
    STORAGE = SERVER.map(PlayerStorageIO(_, skillTypeOps = globalState.SKILL_TYPE_OPS))
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState, globalState.SKILLS)
    PLAYER_OPS = Player(PLAYERS, globalState.SKILL_TYPE_OPS, globalState.SKILL_OPS)
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)
  }

  private[server] def onReload(@unused resourceManager: Option[ResourceManager]): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers).foreach {
      val netHandler = getNetHandler
      _.foreach(netHandler.syncPlayer(_))
    }
  }

  def getNetHandler: NetHandler = NetHandler(PLAYER_OPS, MANAGER.SYNC_SKILLS)
}
