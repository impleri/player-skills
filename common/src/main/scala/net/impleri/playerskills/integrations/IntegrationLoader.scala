package net.impleri.playerskills.integrations

import net.impleri.playerskills.integrations.ftbquests.FtbQuestsIntegration
import net.impleri.playerskills.integrations.ftbteams.FtbTeamsIntegration
import net.impleri.playerskills.integrations.jei.PlayerSkillsJeiPlugin
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.integrations.rei.PlayerSkillsReiPlugin
import net.impleri.slab.platform.ModLookup

case class IntegrationLoader(globalState: StateContainer, serverState: ServerStateContainer) extends ModLookup {
  private var FTB_QUESTS: Option[FtbQuestsIntegration] = None
  private var FTB_TEAMS: Option[FtbTeamsIntegration] = None
  private var JEI: Option[PlayerSkillsJeiPlugin] = None
  private var REI: Option[PlayerSkillsReiPlugin] = None

  def onSetup(): Unit = {
    serverState.setTeam(StubTeam())

    if (isModLoaded("ftbquests")) {
      FTB_QUESTS = Option(FtbQuestsIntegration())
    }

    if (isModLoaded("ftbteams")) {
      FTB_TEAMS = Option(FtbTeamsIntegration(serverState))
    }

    if (isModLoaded("jei")) {
      JEI = Option(PlayerSkillsJeiPlugin(globalState.RESTRICTIONS))
    }

    if (isModLoaded("rei")) {
      REI = Option(new PlayerSkillsReiPlugin(globalState.RESTRICTIONS))
    }
  }
}
