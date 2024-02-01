package net.impleri.playerskills.integrations.ftbteams.facades

import dev.ftb.mods.ftbteams.FTBTeamsAPI

import java.util.UUID
import scala.util.Try

case class FtbTeamsApi() {
  def getTeamOf(player: UUID): Option[Team] = {
    Try(FTBTeamsAPI.getPlayerTeam(player))
      .toOption
      .flatMap(t => Option(t))
      .map(Team)
  }
}
