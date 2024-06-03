package net.impleri.playerskills.bindings.interaction

import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.bindings.interactions.BeforeInteractEntity
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger

class BeforeInteractEntitySpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[InteractionEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = BeforeInteractEntity(
    mockOps,
    mockUpstream,
    mockLogger,
    mockLogger,
  )

  "BeforeInteractEntitySpec.handler" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[Hand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, None) returns false

    val result = testUnit.handler(mockPlayer, Option(mockMob), mockHand)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[Hand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockOps.isUsable(mockPlayer, mockItem, None) returns true
    mockItem.isDefault returns false

    testUnit.handler(mockPlayer, Option(mockMob), mockHand) shouldBe EventResult.pass()
  }

  it should "does nothing to the event if the item is nothing" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[Hand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns Option(mockItem)
    mockItem.isDefault returns true

    testUnit.handler(mockPlayer, Option(mockMob), mockHand) shouldBe EventResult.pass()

    mockOps.isUsable(mockPlayer, mockItem, None) wasNever called
  }
}
