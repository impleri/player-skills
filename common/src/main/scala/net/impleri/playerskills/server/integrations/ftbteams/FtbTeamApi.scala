package net.impleri.playerskills.server.integrations.ftbteams

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import dev.ftb.mods.ftbteams.data.{Team => FtbTeam}
import dev.ftb.mods.ftbteams.data.TeamRank
import net.impleri.playerskills.server.api.Team

import java.util.UUID
import scala.jdk.javaapi.CollectionConverters
import scala.util.Try

case class FtbTeamApi() extends Team {
  private def getTeamOf(player: UUID): Option[FtbTeam] = {
    Try(FTBTeamsAPI.getPlayerTeam(player))
      .toOption
      .flatMap(t => Option(t))
  }

  private def getMembersOf(team: FtbTeam): List[UUID] = {
    CollectionConverters.asScala(team.getRanked(TeamRank.NONE)).keys.toList
  }

  override protected[server] def getTeamMembersFor(player: UUID): List[UUID] = {
    getTeamOf(player)
      .toList
      .flatMap(getMembersOf)
  }
}
