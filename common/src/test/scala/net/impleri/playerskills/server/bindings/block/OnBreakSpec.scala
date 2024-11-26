package net.impleri.playerskills.server.bindings.block

import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.block.Block
import net.impleri.slab.entity.Player
import net.impleri.slab.events.BlockEvents
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Level
import net.impleri.slab.world.Position

class OnBreakSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockUpstream = mock[BlockEvents]
  private val mockLogger = mock[Logger]

  private val testUnit = OnBreak(mockOps, mockUpstream, mockLogger)

  private val mockPlayer = mock[Player]
  private val mockBlock = mock[Block]
  private val mockPos = mock[Position]
  private val mockItem = mock[Item]
  private val mockLevel = mock[Level.Any]
  private val xp: Int = 12
  mockPlayer.getItemInMainHand returns Option(mockItem)

  "OnBreak.handler" should "interrupt the event if item is unusable" in {
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns false

    val result = testUnit.handler(mockPlayer, Option(mockBlock), Option(mockPos), Option(mockLevel), Option(xp))

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "do nothing to the event if item is usable" in {
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns true

    testUnit.handler(mockPlayer, Option(mockBlock), Option(mockPos), Option(mockLevel), Option(xp)) shouldBe EventResult
      .pass()
  }
}
