package net.impleri.playerskills.forge

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.integrations.curios.forge.CuriosForgeIntegration
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.platform.ModLookup

case class ForgeIntegrationLoader(
  globalState: StateContainer,
  commonLifecycle: CommonLifecycleEvents = CommonLifecycleEvents(),
)
  extends ModLookup {
  private var CURIOS: Option[CuriosForgeIntegration] = None

  private def handle(): Unit = {
    if (isModLoaded("curios")) {
      CURIOS = Option(CuriosForgeIntegration(globalState.ITEM_RESTRICTIONS))
    }
  }

  commonLifecycle.onSetup(handle)
}
