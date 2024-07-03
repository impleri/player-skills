package net.impleri.playerskills.server.bindings.lifecycle

import net.impleri.playerskills.integrations.ftbquests.FtbQuestsIntegration
import net.impleri.playerskills.integrations.ftbteams.FtbTeamsIntegration
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.StateContainer
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.platform.ModLookup

case class OnSetup(
  globalState: StateContainer,
  serverState: ServerStateContainer,
  upstream: CommonLifecycleEvents = CommonLifecycleEvents(),
) extends ModLookup {
  private var FTB_QUESTS: Option[FtbQuestsIntegration] = None
  private var FTB_TEAMS: Option[FtbTeamsIntegration] = None

  private def handle(): Unit = {
    serverState.setTeam(StubTeam())

    if (isModLoaded("ftbquests")) {
      FTB_QUESTS = Option(FtbQuestsIntegration())
    }

    if (isModLoaded("ftbteams")) {
      FTB_TEAMS = Option(FtbTeamsIntegration(serverState))
    }
  }

  upstream.onSetup(handle)
}
