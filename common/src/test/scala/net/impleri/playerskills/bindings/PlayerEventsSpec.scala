package net.impleri.playerskills.bindings

import dev.architectury.event.Event
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

class PlayerEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockPickup = mock[Event[PlayerEvent.PickupItemPredicate]]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = PlayerEvents(
    mockOps,
    mockPickup,
    mockLogger,
  )

  "PlayerEvents.registerEvents" should "bind interaction events" in {
    testUnit.registerEvents()

    mockPickup.register(*) wasCalled once
  }

  "PlayerEvents.beforePlayerPickup" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockItem = mock[Item]

    mockOps.isHoldable(mockPlayer, mockItem) returns false

    val result = testUnit.beforePlayerPickup(mockPlayer, mockItem)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockItem = mock[Item]

    mockOps.isHoldable(mockPlayer, mockItem) returns true

    testUnit.beforePlayerPickup(mockPlayer, mockItem) shouldBe EventResult.pass()
  }
}
