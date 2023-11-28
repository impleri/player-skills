package net.impleri.playerskills.bindings

import dev.architectury.event.Event
import net.impleri.playerskills.BaseSpec

class LifecycleEventsSpec extends BaseSpec {
  private val eventMock = mock[Event[Runnable]]
  private val onSetupMock = mock[() => Unit]

  private val testUnit = LifecycleEvents(onSetupMock, eventMock)

  "LifecycleEvents.registerEvents" should "bind common events" in {
    testUnit.registerEvents()

    eventMock.register(*) wasCalled once
  }

  "LifecycleEvents.setup" should "proxies onSetup method" in {
    testUnit.setup()

    onSetupMock() wasCalled once
  }
}
