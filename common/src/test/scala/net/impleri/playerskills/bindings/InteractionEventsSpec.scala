package net.impleri.playerskills.bindings

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.logging.Logger

class InteractionEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val callback = mock[() => Unit]
  private val mockCommonUpstream = mock[CommonLifecycleEvents]
  private val mockInteraction = mock[InteractionEvents]
  private val mockPlayer = mock[PlayerEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = EventBindings(
    mockOps,
    callback,
    mockCommonUpstream,
    mockInteraction,
    mockPlayer,
    mockLogger,
    mockLogger,
  )

  "InteractionEvents.registerEvents" should "bind interaction events" in {
    testUnit.registerEvents()

    mockCommonUpstream.onSetup(callback) wasCalled once
    mockInteraction.onRightClickItem(*) wasCalled once
    mockInteraction.onRightClickBlock(*) wasCalled once
    mockInteraction.onRightClickEntity(*) wasCalled once
    mockPlayer.canPickup(*) wasCalled once
  }
}
