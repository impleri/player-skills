package net.impleri.playerskills.server

import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.skills.PlayerStorageIO
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.StateContainer
import net.minecraft.server.packs.resources.ResourceManager

import scala.annotation.unused

/**
 * Single place for all stateful classes
 */
case class ServerStateContainer(
  globalState: StateContainer = PlayerSkills.STATE,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
) {
  var SERVER: Option[MinecraftServer] = None
  private var STORAGE: Option[PlayerStorageIO] = None
  var PLAYERS: PlayerRegistry = PlayerRegistry()
  var TEAM: Team = StubTeam()

  val EVENT_HANDLERS: EventHandlers = EventHandlers(onServerChange = onServerChange, onReloadResources = onReload)

  def init(): Unit = logger.info("PlayerSkills Server Loaded")

  def setTeam(instance: Team): Unit = TEAM = instance

  private[server] def onServerChange(next: Option[MinecraftServer] = None): Unit = {
    SERVER = next
    STORAGE = next.map(PlayerStorageIO(_))
    PLAYERS = PlayerRegistry(STORAGE, PLAYERS.getState, globalState.SKILLS)
  }

  private[server] def onReload(@unused resourceManager: ResourceManager): Unit = {
    val playerList = PLAYERS.close()
    PLAYERS.open(playerList)

    SERVER.map(_.getPlayers)
      .foreach(_.foreach(getNetHandler.syncPlayer(_)))
  }

  def getPlayerOps: Player = Player(PLAYERS, globalState.getSkillTypeOps, globalState.getSkillOps)

  def getTeamOps: TeamOps = Team(TEAM, getPlayerOps, globalState.getSkillOps, EVENT_HANDLERS)

  def getNetHandler: NetHandler = NetHandler(getPlayerOps)
}
