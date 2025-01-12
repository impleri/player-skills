package net.impleri.playerskills.server

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.data.restrictions.{ItemRestrictionDataLoader, RecipeRestrictionDataLoader}
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

import java.util.UUID
import scala.annotation.unused

/** Single place for all stateful classes
  */
case class ServerStateContainer(
  private val globalState: StateContainer = StateContainer(),
  PLAYERS: PlayerRegistry = PlayerRegistry(),
  private val eventHandler: EventHandler = EventHandler(),
  private val reloadListeners: ReloadListeners = ReloadListeners(true),
  var TEAM: Team = StubTeam(),
  var SERVER: Option[Server] = None,
  private val itemRegistry: Registry.ITEM = Registry.Items,
  private val logger: Logger = PlayerSkillsLogger.SKILLS,
) {
  private var STORAGE: Option[PlayerStorageIO] =
    SERVER.map(PlayerStorageIO(_, globalState.SKILL_TYPE_OPS))

  val PLAYER_OPS: Player =
    Player(PLAYERS, globalState.SKILL_TYPE_OPS, globalState.SKILL_OPS)
  private val TEAM_OPS: TeamOps =
    Team(TEAM, PLAYER_OPS, globalState.SKILL_OPS, eventHandler)

  private lazy val MANAGER =
    Manager(globalState, serverStateContainer = Option(this))

  private lazy val itemRestrictions = ItemRestrictionDataLoader(
    ItemRestrictionBuilder(Option(itemRegistry), globalState.RESTRICTIONS),
    globalState.SKILL_OPS,
    globalState.SKILL_TYPE_OPS,
    PLAYER_OPS,
  )

  private lazy val recipeRestrictions = RecipeRestrictionDataLoader(
    RecipeRestrictionBuilder(this, globalState.RESTRICTIONS),
    globalState.SKILL_OPS,
    globalState.SKILL_TYPE_OPS,
    PLAYER_OPS,
  )

  private val EVENT_BINDINGS = ServerEventBindings(
    globalState,
    this,
    onServerChange,
    () =>
      PlayerSkillsCommands(
        globalState.SKILL_OPS,
        globalState.SKILL_TYPE_OPS,
        PLAYER_OPS,
        TEAM_OPS,
        globalState.RESTRICTIONS,
      ),
    getNetHandler,

  )

  private val INTERNAL = InternalEvents(
    itemRestrictions,
    recipeRestrictions,
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
    TEAM_OPS.changeTeam(TEAM)
  }

  private[server] def onServerChange(next: Option[Server] = None): Unit = {
    val playerList = PLAYERS.close()
    SERVER = next
    STORAGE = SERVER.map(PlayerStorageIO(_, globalState.SKILL_TYPE_OPS))
    PLAYERS.changeStorage(STORAGE)
    logger.info("Resyncing players after server change")
    resync(playerList)
  }

  private[server] def onReload(
    @unused resourceManager: Option[ResourceManager],
  ): Unit = {
    resync(PLAYERS.close())

  }

  private[server] def resync(playerList: List[UUID]): Unit = {
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers).foreach {
      val netHandler = getNetHandler
      _.foreach(netHandler.syncPlayer(_))
    }
  }

  def getNetHandler: NetHandler = NetHandler(PLAYER_OPS, MANAGER.SYNC_SKILLS)
}
