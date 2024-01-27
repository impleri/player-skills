package net.impleri.playerskills.server.bindings

import dev.architectury.event.Event
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Block
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

class BlockEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockEvent = mock[Event[BlockEvent.Break]]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = BlockEvents(mockOps, mockEvent, mockLogger, mockLogger)

  private val mockPlayer = mock[Player[_]]
  private val mockBlock = mock[Block]
  private val mockPos = mock[Position]
  private val mockItem = mock[Item]
  mockPlayer.getItemInMainHand returns mockItem

  "BlockEvents.registerEvents" should "register event handlers" in {
    testUnit.registerEvents()

    mockEvent.register(*) wasCalled once
  }

  "BlockEvents.beforeMine" should "interrupt the event if item is unusable" in {
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns false

    val result = testUnit.beforeMine(mockPlayer, mockBlock, mockPos)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "do nothing to the event if item is usable" in {
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns true

    testUnit.beforeMine(mockPlayer, mockBlock, mockPos) shouldBe EventResult.pass()
  }
}
