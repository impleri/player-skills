package net.impleri.playerskills.integrations

import dev.architectury.platform.Platform
import net.impleri.playerskills.integrations.ftbquests.FtbQuestsIntegration
import net.impleri.playerskills.integrations.ftbteams.FtbTeamsIntegration
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.server.api.StubTeam

case class IntegrationLoader(serverState: ServerStateContainer) {
  private var FTB_QUESTS: Option[FtbQuestsIntegration] = None
  private var FTB_TEAMS: Option[FtbTeamsIntegration] = None

  def onSetup(): Unit = {
    serverState.setTeam(StubTeam())

    if (Platform.isModLoaded("ftbquests")) {
      FTB_QUESTS = Option(FtbQuestsIntegration())
    }

    if (Platform.isModLoaded("ftbteams")) {
      FTB_TEAMS = Option(FtbTeamsIntegration(serverState))
    }
  }
}
