package net.impleri.playerskills.fabric

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.integrations.trinkets.fabric.TrinketsFabricIntegration
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.platform.ModLookup

case class FabricIntegrationLoader(
  globalState: StateContainer,
  commonLifecycle: CommonLifecycleEvents = CommonLifecycleEvents(),
)
  extends ModLookup {
  private var TRINKETS: Option[TrinketsFabricIntegration] = None

  private def handle(): Unit = {
    if (isModLoaded("trinkets")) {
      TRINKETS = Option(TrinketsFabricIntegration(globalState.ITEM_RESTRICTIONS))
    }
  }

  commonLifecycle.onSetup(handle)
}
