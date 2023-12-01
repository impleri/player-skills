package net.impleri.playerskills

case class IntegrationLoader() {

  def onSetup(): Unit = {
    //    if (Platform.isModLoaded("ftbquests")) {
    //      net.impleri.playerskills.integrations.ftbquests.PlayerSkillsIntegration.init()
    //    }

    // Enable FTB Teams integration if the mod is there
    //    if (Platform.isModLoaded("ftbteams")) {
    //      net.impleri.playerskills.integrations.ftbteams.FTBTeamsPlugin.registerInstance()
    //    }
  }
}
