package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.{Player => PlayerFacade}
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.player.Player

class ItemRestrictionOpsSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = ItemRestrictionOps(mockRegistry, mockLogger)

  private val mockPlayer = mock[PlayerFacade[Player]]
  private val mockEntity = mock[Entity[Player]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockTarget = mock[Item]

  private val testRestriction = ItemRestriction(mockTarget)

  mockEntity.asPlayer[Player] returns mockPlayer

  mockPlayer.asOption returns Option(mockEntity)
  mockPlayer.dimension returns None
  mockPlayer.biomeAt(None) returns None

  mockTarget.getName returns Option(mockTargetName)

  "ItemRestrictionOps.isIdentifiable" should "return false if a restriction has identifiable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(identifiable = testValue))
    testUnit.isIdentifiable(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have identifiable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isIdentifiable(mockPlayer, mockTarget) shouldBe true
  }

  "ItemRestrictionOps.isHoldable" should "return false if a restriction has holdable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(holdable = testValue))
    testUnit.isHoldable(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have holdable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isHoldable(mockPlayer, mockTarget) shouldBe true
  }

  "ItemRestrictionOps.isWearable" should "return false if a restriction has wearable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(wearable = testValue))
    testUnit.isWearable(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have wearable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isWearable(mockPlayer, mockTarget) shouldBe true
  }

  "ItemRestrictionOps.isUsable" should "return false if a restriction has usable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(usable = testValue))
    testUnit.isUsable(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have usable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isUsable(mockPlayer, mockTarget) shouldBe true
  }

  "ItemRestrictionOps.isHarmful" should "return false if a restriction has harmful = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(harmful = testValue))
    testUnit.isHarmful(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have harmful = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isHarmful(mockPlayer, mockTarget) shouldBe true
  }
}
