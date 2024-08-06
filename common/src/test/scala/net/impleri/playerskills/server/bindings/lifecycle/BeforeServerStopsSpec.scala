package net.impleri.playerskills.server.bindings.lifecycle

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.slab.events.ServerLifecycleEvents

class BeforeServerStopsSpec extends BaseSpec {
  private val mockPlayerRegistry = mock[PlayerRegistry]
  private val mockUpstream = mock[ServerLifecycleEvents]

  private val testUnit = BeforeServerStops(
    () => mockPlayerRegistry,
    mockUpstream
  )

  "BeforeServerStops.handler" should "handle stop" in {
    testUnit.handler(None)

    mockPlayerRegistry.close() wasCalled once
  }
}
