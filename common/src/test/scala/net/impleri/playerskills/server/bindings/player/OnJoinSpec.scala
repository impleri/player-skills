package net.impleri.playerskills.server.bindings.player

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.NetHandler
import net.impleri.slab.entity.Player
import net.impleri.slab.events.PlayerEvents

import java.util.UUID

class OnJoinSpec extends BaseSpec {
  private val mockRegistry = mock[PlayerRegistry]
  private val mockNet = mock[NetHandler]
  private val mockUpstream = mock[PlayerEvents]

  private val testUnit = OnJoin(() => mockRegistry, mockNet, mockUpstream)

  private val mockPlayer = mock[Player[_]]
  private val mockUuid = mock[UUID]
  mockPlayer.uuid returns mockUuid

  "OnJoin.handler" should "register event handlers" in {
    testUnit.handler(mockPlayer)
    mockRegistry.open(mockUuid) wasCalled once
    mockNet.syncPlayer(mockPlayer) wasCalled once
  }
}
