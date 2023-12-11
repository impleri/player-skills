package net.impleri.playerskills.server

import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.network.Manager
import net.impleri.playerskills.server.bindings.CommandEvents
import net.impleri.playerskills.server.bindings.InternalEvents
import net.impleri.playerskills.server.bindings.LifecycleEvents
import net.impleri.playerskills.server.commands.PlayerSkillsCommands
import net.minecraft.server.packs.resources.ResourceManager

import scala.annotation.unused

/**
 * Single place for all stateful classes
 */
case class ServerStateContainer(
  globalState: StateContainer = StateContainer(),
  playerRegistry: PlayerRegistry = PlayerRegistry(),
  eventHandler: EventHandler = EventHandler(),
  reloadListeners: ReloadListeners = ReloadListeners(true),
  teamInstance: Team = StubTeam(),
  server: Option[Server] = None,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  var SERVER: Option[Server] = server
  private var STORAGE: Option[PlayerStorageIO] = SERVER.map(
    PlayerStorageIO(_, skillTypeOps = globalState.getSkillTypeOps),
  )

  var PLAYERS: PlayerRegistry = playerRegistry
  var TEAM: Team = teamInstance

  var PLAYER_OPS: Player = Player(PLAYERS, globalState.getSkillTypeOps, globalState.getSkillOps)
  var TEAM_OPS: TeamOps = Team(TEAM, PLAYER_OPS, globalState.getSkillOps, eventHandler)

  lazy private val MANAGER = Manager(globalState, serverStateContainer = this)

  private val LIFECYCLE = LifecycleEvents(PLAYERS, onServerChange)
  private val INTERNAL = InternalEvents(eventHandler, this, onReload, reloadListeners)
  private val COMMAND = CommandEvents()

  LIFECYCLE.registerEvents()
  INTERNAL.registerEvents()
  COMMAND.registerEvents(
    PlayerSkillsCommands(
      globalState.getSkillOps,
      globalState.getSkillTypeOps,
      PLAYER_OPS,
      TEAM_OPS,
    ),
  )

  logger.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = {
    TEAM = instance
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.getSkillOps, eventHandler)
  }

  private[server] def onServerChange(next: Option[Server] = None): Unit = {
    SERVER = next
    STORAGE = SERVER.map(PlayerStorageIO(_, skillTypeOps = globalState.getSkillTypeOps))
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState, globalState.SKILLS)
    PLAYER_OPS = Player(PLAYERS, globalState.getSkillTypeOps, globalState.getSkillOps)
    TEAM_OPS = Team(TEAM, PLAYER_OPS, globalState.getSkillOps, eventHandler)
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
