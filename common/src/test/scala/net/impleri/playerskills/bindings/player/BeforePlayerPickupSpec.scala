package net.impleri.playerskills.bindings.player

import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Player
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger

class BeforePlayerPickupSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[PlayerEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = BeforePlayerPickup(
    mockOps,
    mockUpstream,
    mockLogger,
  )

  "BeforePlayerPickup.handler" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player]
    val mockItem = mock[Item]
    val mockItemEntity = mock[Entity.Any]

    mockOps.isHoldable(mockPlayer, mockItem) returns false

    val result = testUnit.handler(mockPlayer, Option(mockItem), Option(mockItemEntity))

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player]
    val mockItem = mock[Item]
    val mockItemEntity = mock[Entity.Any]

    mockOps.isHoldable(mockPlayer, mockItem) returns true

    testUnit.handler(mockPlayer, Option(mockItem), Option(mockItemEntity)) shouldBe EventResult.pass()
  }
}
