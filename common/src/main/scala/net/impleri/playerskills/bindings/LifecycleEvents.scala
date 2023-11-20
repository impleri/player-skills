package net.impleri.playerskills.bindings

import dev.architectury.event.events.common.LifecycleEvent

case class LifecycleEvents(onSetup: () => Unit = () => {}) {
  def registerEvents(): Unit = {
    LifecycleEvent.SETUP.register(() => setup())
  }

  private def setup(): Unit = {
    onSetup()
  }
}
