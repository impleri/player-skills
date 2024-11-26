package net.impleri.playerskills.bindings.interaction

import dev.architectury.event.CompoundEventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.bindings.interactions.BeforeUseItem
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger

class BeforeUseItemSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[InteractionEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = BeforeUseItem(
    mockOps,
    mockUpstream,
    mockLogger,
  )

  "BeforeUseItem.handler" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player]
    val mockHand = mock[Hand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockOps.isUsable(mockPlayer, mockItem, None) returns false

    val result = testUnit.handler(mockPlayer, mockHand)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player]
    val mockHand = mock[Hand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockOps.isUsable(mockPlayer, mockItem, None) returns true

    testUnit.handler(mockPlayer, mockHand) shouldBe CompoundEventResult.pass()
  }
}
