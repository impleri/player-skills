package net.impleri.playerskills.integrations.ftbteams

import net.impleri.playerskills.integrations.ftbteams.facades.FtbTeamsApi
import net.impleri.playerskills.server.api.Team

import java.util.UUID

case class FtbTeamApi(ftbTeamApi: FtbTeamsApi = FtbTeamsApi()) extends Team {
  override protected[playerskills] def getTeamMembersFor(player: UUID): List[UUID] = {
    ftbTeamApi.getTeamOf(player)
      .toList
      .flatMap(_.getAllMembers)
  }
}
