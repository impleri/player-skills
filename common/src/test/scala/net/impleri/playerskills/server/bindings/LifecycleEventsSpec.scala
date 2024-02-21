package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.Event
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.server.skills.PlayerRegistry

class LifecycleEventsSpec extends BaseSpec {
  private val setupEventMock = mock[Event[Runnable]]
  private val eventMock = mock[Event[LifecycleEvent.ServerState]]
  private val playerRegistryMock = mock[PlayerRegistry]
  private val onSetupMock = mock[() => Unit]
  private val onChangeMock = mock[Option[Server] => Unit]

  private val testUnit = LifecycleEvents(
    playerRegistryMock,
    onSetupMock,
    onChangeMock,
    setupEventMock,
    eventMock,
    eventMock,
  )

  "LifecycleEvents.registerEvents" should "bind events" in {
    testUnit.registerEvents()

    eventMock.register(*) wasCalled twice
    setupEventMock.register(*) wasCalled once
  }

  "LifecycleEvents.onSetup" should "proxies the passed callback" in {
    testUnit.onSetup()

    onSetupMock() wasCalled once
  }

  "LifecycleEvents.beforeServerStart" should "proxies the passed callback" in {
    val serverMock = mock[Server]

    testUnit.beforeServerStart(serverMock)

    onChangeMock(Option(serverMock)) wasCalled once
  }

  "LifecycleEvents.beforeServerStops" should "handle stop" in {
    testUnit.beforeServerStops()

    playerRegistryMock.close() wasCalled once
    onChangeMock(None) wasCalled once
  }
}
