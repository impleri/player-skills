package net.impleri.playerskills.bindings

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.Event

case class LifecycleEvents(onSetup: () => Unit = () => {}, setupEvent: Event[Runnable] = LifecycleEvent.SETUP) {
  def registerEvents(): Unit = {
    setupEvent.register(() => setup())
  }

  private[bindings] def setup(): Unit = {
    onSetup()
  }
}
