package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.LifecycleEvent

case class CommonLifecycleEvents(
  private val onSetupEvent: Event[Runnable] = LifecycleEvent.SETUP,
) {
  def onSetup(handler: CommonLifecycleEvents.OnSetup): Unit = {
    onSetupEvent.register(() => handler())
  }
}

object CommonLifecycleEvents {
  type OnSetup = () => Unit
}
