package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.server.EventHandler
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.minecraft.server.packs.resources.ResourceManager

class InternalEventsSpec extends BaseSpec {
  private val eventHandlerMock = mock[EventHandler]
  private val stateMock = mock[ServerStateContainer]
  private val reloadMock = mock[ReloadListeners]
  private val reload = mock[ResourceManager => Unit]

  private val testUnit = InternalEvents(
    eventHandlerMock,
    stateMock,
    reload,
    reloadMock,
  )

  "InternalEvents.registerEvents" should "bind callbacks to events" in {
    testUnit.registerEvents()

    eventHandlerMock.onSkillChanged(*) wasCalled once
    reloadMock.register(*) wasCalled twice
  }

  "InternalEvents.onResourceManagerReload" should "proxy calls to the reload listener" in {
    val managerMock = mock[ResourceManager]
    testUnit.onResourceManagerReload(managerMock)

    reload(*) wasCalled once
  }

  "InternalEvents.onSkillChanged" should "resync the player" in {
    val eventMock = mock[SkillChangedEvent[_]]

    val netMock = mock[NetHandler]

    stateMock.getNetHandler returns netMock

    testUnit.onSkillChanged(eventMock)

    netMock.syncPlayer(eventMock) wasCalled once
  }
}
