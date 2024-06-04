package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.item.ItemRestriction
import net.impleri.slab.client.Client
import net.impleri.slab.entity.{Player => PlayerFacade}
import net.impleri.slab.entity.Entity
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.client.player.LocalPlayer

class ItemRestrictionOpsClientSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockClient = mock[Client]
  private val mockLogger = mock[Logger]

  private val testUnit = ItemRestrictionOpsClient(mockRegistry, mockClient, mockLogger)

  private val mockPlayer = mock[PlayerFacade[LocalPlayer]]
  private val mockEntity = mock[Entity[LocalPlayer]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockTarget = mock[Item]

  private val testRestriction = ItemRestriction(mockTarget)

  mockClient.getPlayer returns Option(mockPlayer)

  mockEntity.asPlayer[LocalPlayer] returns mockPlayer

  mockPlayer.asOption returns Option(mockEntity)
  mockPlayer.dimension returns None
  mockPlayer.biomeAt(None) returns None

  mockTarget.name returns Option(mockTargetName)

  "ItemRestrictionOpsClient.isIdentifiable" should "return false if a restriction has identifiable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(identifiable = testValue))
    testUnit.isIdentifiable(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have identifiable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isIdentifiable(mockTarget, None) shouldBe true
  }

  "ItemRestrictionOpsClient.isHoldable" should "return false if a restriction has holdable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(holdable = testValue))
    testUnit.isHoldable(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have holdable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isHoldable(mockTarget, None) shouldBe true
  }

  "ItemRestrictionOpsClient.isWearable" should "return false if a restriction has wearable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(wearable = testValue))
    testUnit.isWearable(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have wearable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isWearable(mockTarget, None) shouldBe true
  }

  "ItemRestrictionOpsClient.isUsable" should "return false if a restriction has usable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(usable = testValue))
    testUnit.isUsable(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have usable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isUsable(mockTarget, None) shouldBe true
  }

  "ItemRestrictionOpsClient.isHarmful" should "return false if a restriction has harmful = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(harmful = testValue))
    testUnit.isHarmful(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have harmful = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isHarmful(mockTarget, None) shouldBe true
  }
}
