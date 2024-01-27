package net.impleri.playerskills.server.bindings

import dev.architectury.event.Event
import dev.architectury.event.events.common.PlayerEvent
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.NetHandler

import java.util.UUID

class PlayerEventsSpec extends BaseSpec {
  private val mockRegistry = mock[PlayerRegistry]
  private val mockNet = mock[NetHandler]
  private val mockJoin = mock[Event[PlayerEvent.PlayerJoin]]
  private val mockQuit = mock[Event[PlayerEvent.PlayerQuit]]

  private val testUnit = PlayerEvents(mockRegistry, mockNet, mockJoin, mockQuit)

  private val mockPlayer = mock[Player[_]]
  private val mockUuid = mock[UUID]
  mockPlayer.uuid returns mockUuid

  "PlayerEvents.registerEvents" should "register event handlers" in {
    testUnit.registerEvents()

    mockJoin.register(*) wasCalled once
    mockQuit.register(*) wasCalled once
  }

  "PlayerEvents.playerJoin" should "register event handlers" in {
    testUnit.playerJoin(mockPlayer)
    mockRegistry.open(mockUuid) wasCalled once
    mockNet.syncPlayer(mockPlayer) wasCalled once
  }

  "PlayerEvents.playerQuit" should "register event handlers" in {
    testUnit.playerQuit(mockPlayer)
    mockRegistry.close(mockUuid) wasCalled once
  }
}
