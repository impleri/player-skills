package net.impleri.playerskills.integrations

import dev.architectury.platform.Platform
import net.impleri.playerskills.integrations.ftbquests.FtbQuestsIntegration

case class IntegrationLoader() {
  private var QUESTS: Option[FtbQuestsIntegration] = None

  def onSetup(): Unit = {
    if (Platform.isModLoaded("ftbquests")) {
      QUESTS = Option(FtbQuestsIntegration())
    }

    // Enable FTB Teams integration if the mod is there
    //    if (Platform.isModLoaded("ftbteams")) {
    //      net.impleri.playerskills.integrations.ftbteams.FTBTeamsPlugin.registerInstance()
    //    }
  }
}
