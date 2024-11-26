package net.impleri.playerskills.bindings.interaction

import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.bindings.interactions.BeforeUseItemBlock
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Direction.Direction
import net.impleri.slab.world.Position

class BeforeUseItemBlockSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[InteractionEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = BeforeUseItemBlock(
    mockOps,
    mockUpstream,
    mockLogger,
  )

  "BeforeUseItemBlock.handler" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player]
    val mockHand = mock[Hand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]
    val mockDirection = mock[Direction]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns false

    val result = testUnit.handler(mockPlayer, Option(mockPos), mockHand, mockDirection)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player]
    val mockHand = mock[Hand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]
    val mockDirection = mock[Direction]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns true

    testUnit.handler(mockPlayer, Option(mockPos), mockHand, mockDirection) shouldBe EventResult.pass()
  }

  it should "does nothing to the event if the item is nothing" in {
    val mockPlayer = mock[Player]
    val mockHand = mock[Hand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]
    val mockDirection = mock[Direction]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockItem.isDefault returns true

    testUnit.handler(mockPlayer, Option(mockPos), mockHand, mockDirection) shouldBe EventResult.pass()

    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) wasNever called
  }
}
