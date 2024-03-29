package net.impleri.playerskills.server.integrations.ftbteams

import net.impleri.playerskills.server.ServerStateContainer

case class FtbTeamsIntegration(serverState: ServerStateContainer) {
  private def getApi: FtbTeamApi = FtbTeamApi()

  serverState.setTeam(getApi)
}
