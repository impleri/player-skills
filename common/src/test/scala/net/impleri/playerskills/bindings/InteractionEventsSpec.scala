package net.impleri.playerskills.bindings

import dev.architectury.event.Event
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.InteractionHand

class InteractionEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockLeftClick = mock[Event[InteractionEvent.LeftClickBlock]]
  private val mockRightClick = mock[Event[InteractionEvent.RightClickBlock]]
  private val mockRightClickItem = mock[Event[InteractionEvent.RightClickItem]]
  private val mockEntity = mock[Event[InteractionEvent.InteractEntity]]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = InteractionEvents(
    mockOps,
    mockLeftClick,
    mockRightClick,
    mockRightClickItem,
    mockEntity,
    mockLogger,
  )

  "InteractionEvents.registerEvents" should "bind interaction events" in {
    testUnit.registerEvents()

    mockLeftClick.register(*) wasCalled once
    mockRightClick.register(*) wasCalled once
    mockRightClickItem.register(*) wasCalled once
    mockEntity.register(*) wasCalled once
  }

  "InteractionEvents.beforeUseItem" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockHand = mock[InteractionHand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockOps.isUsable(mockPlayer, mockItem, None) returns false

    val result = testUnit.beforeUseItem(mockPlayer, mockHand)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockHand = mock[InteractionHand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockOps.isUsable(mockPlayer, mockItem, None) returns true

    testUnit.beforeUseItem(mockPlayer, mockHand) shouldBe CompoundEventResult.pass()
  }

  "InteractionEvents.beforeUseItemBlock" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockHand = mock[InteractionHand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns false

    val result = testUnit.beforeUseItemBlock(mockPlayer, mockHand, mockPos)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockHand = mock[InteractionHand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) returns true

    testUnit.beforeUseItemBlock(mockPlayer, mockHand, mockPos) shouldBe EventResult.pass()
  }

  it should "does nothing to the event if the item is nothing" in {
    val mockPlayer = mock[Player[_]]
    val mockHand = mock[InteractionHand]
    val mockPos = mock[Position]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockItem.isDefault returns true

    testUnit.beforeUseItemBlock(mockPlayer, mockHand, mockPos) shouldBe EventResult.pass()

    mockOps.isUsable(mockPlayer, mockItem, Option(mockPos)) wasNever called
  }

  "InteractionEvents.beforeInteractEntity" should "interrupts the event if restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[InteractionHand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockItem.isDefault returns false
    mockOps.isUsable(mockPlayer, mockItem, None) returns false

    val result = testUnit.beforeInteractEntity(mockPlayer, mockMob, mockHand)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "does nothing to the event if not restricted" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[InteractionHand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockOps.isUsable(mockPlayer, mockItem, None) returns true
    mockItem.isDefault returns false

    testUnit.beforeInteractEntity(mockPlayer, mockMob, mockHand) shouldBe EventResult.pass()
  }

  it should "does nothing to the event if the item is nothing" in {
    val mockPlayer = mock[Player[_]]
    val mockMob = mock[Entity[_]]
    val mockHand = mock[InteractionHand]
    val mockItem = mock[Item]

    mockPlayer.getItemInHand(mockHand) returns mockItem
    mockItem.isDefault returns true

    testUnit.beforeInteractEntity(mockPlayer, mockMob, mockHand) shouldBe EventResult.pass()

    mockOps.isUsable(mockPlayer, mockItem, None) wasNever called
  }
}
