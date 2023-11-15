package net.impleri.playerskills.server

import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.StateContainer
import net.minecraft.server.packs.resources.ResourceManager

import scala.annotation.unused

/**
 * Single place for all stateful classes
 */
case class ServerStateContainer(
  playerRegistry: PlayerRegistry = PlayerRegistry(),
  globalState: StateContainer = StateContainer(),
  teamInstance: Team = StubTeam(),
  server: Option[MinecraftServer] = None,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  var SERVER: Option[MinecraftServer] = server
  var PLAYERS: PlayerRegistry = playerRegistry
  var TEAM: Team = teamInstance
  private var STORAGE: Option[PlayerStorageIO] = getPlayerStorageIO

  val EVENT_HANDLERS: EventHandlers = EventHandlers(onServerChange = onServerChange, onReloadResources = onReload)

  def init(): Unit = logger.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = TEAM = instance

  private[server] def onServerChange(next: Option[MinecraftServer] = None): Unit = {
    SERVER = next
    STORAGE = getPlayerStorageIO
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState, globalState.SKILLS)
  }

  private[server] def onReload(@unused resourceManager: ResourceManager): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers)
      .foreach { players =>
        val netHandler = getNetHandler
        players.foreach(netHandler.syncPlayer(_))
      }
  }

  private def getPlayerStorageIO: Option[PlayerStorageIO] = {
    SERVER
      .map(PlayerStorageIO(_, skillTypeOps = globalState.getSkillTypeOps))
  }

  def getPlayerOps: Player = Player(PLAYERS, globalState.getSkillTypeOps, globalState.getSkillOps)

  def getTeamOps: TeamOps = Team(TEAM, getPlayerOps, globalState.getSkillOps, EVENT_HANDLERS)

  def getNetHandler: NetHandler = NetHandler(getPlayerOps)
}
