package net.impleri.playerskills.server.integrations

import dev.architectury.platform.Platform
import net.impleri.playerskills.server.integrations.ftbquests.FtbQuestsIntegration
import net.impleri.playerskills.server.integrations.ftbteams.FtbTeamsIntegration
import net.impleri.playerskills.server.ServerStateContainer

case class IntegrationLoader(serverState: ServerStateContainer) {
  private var FTB_QUESTS: Option[FtbQuestsIntegration] = None
  private var FTB_TEAMS: Option[FtbTeamsIntegration] = None

  def onSetup(): Unit = {
    if (Platform.isModLoaded("ftbquests")) {
      FTB_QUESTS = Option(FtbQuestsIntegration())
    }

    // Enable FTB Teams integration if the mod is there
    if (Platform.isModLoaded("ftbteams")) {
      FTB_TEAMS = Option(FtbTeamsIntegration(serverState))
    }
  }
}
