package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.ResourceLocation

class ItemConditionsSpec extends BaseSpec {
  private case class TestConditions() extends ItemConditions {
    override def name: ResourceLocation = new ResourceLocation("skillstest", "condition")
  }

  private val testUnit = TestConditions()

  "ItemConditions.holdable" should "ensure isHoldable is true" in {
    testUnit.isHoldable shouldBe None
    testUnit.holdable()
    testUnit.isHoldable.value shouldBe true
  }

  "ItemConditions.unholdable" should "ensure isHoldable is false" in {
    testUnit.isHoldable shouldBe None
    testUnit.unholdable()
    testUnit.isHoldable.value shouldBe false
    testUnit.isWearable.value shouldBe false
    testUnit.isUsable.value shouldBe false
  }

  "ItemConditions.identifiable" should "ensure isIdentifiable is true" in {
    testUnit.isIdentifiable shouldBe None
    testUnit.identifiable()
    testUnit.isIdentifiable.value shouldBe true
  }

  "ItemConditions.unidentifiable" should "ensure isIdentifiable is false" in {
    testUnit.isIdentifiable shouldBe None
    testUnit.unidentifiable()
    testUnit.isIdentifiable.value shouldBe false
  }

  "ItemConditions.harmful" should "ensure isHarmful is true" in {
    testUnit.isHarmful shouldBe None
    testUnit.harmful()
    testUnit.isHarmful.value shouldBe true
    testUnit.isHoldable.value shouldBe true
  }

  "ItemConditions.harmless" should "ensure isHarmful is false" in {
    testUnit.isHarmful shouldBe None
    testUnit.harmless()
    testUnit.isHarmful.value shouldBe false
  }

  "ItemConditions.wearable" should "ensure isWearable is true" in {
    testUnit.isWearable shouldBe None
    testUnit.wearable()
    testUnit.isWearable.value shouldBe true
    testUnit.isHoldable.value shouldBe true
  }

  "ItemConditions.unwearable" should "ensure isWearable is false" in {
    testUnit.isWearable shouldBe None
    testUnit.unwearable()
    testUnit.isWearable.value shouldBe false
  }

  "ItemConditions.usable" should "ensure isUsable is true" in {
    testUnit.isUsable shouldBe None
    testUnit.usable()
    testUnit.isUsable.value shouldBe true
    testUnit.isHoldable.value shouldBe true
  }

  "ItemConditions.unusable" should "ensure isUsable is false" in {
    testUnit.isUsable shouldBe None
    testUnit.unusable()
    testUnit.isUsable.value shouldBe false
  }

  "ItemConditions.nothing" should "ensure everything is true" in {
    testUnit.isHarmful shouldBe None
    testUnit.isHoldable shouldBe None
    testUnit.isIdentifiable shouldBe None
    testUnit.isUsable shouldBe None
    testUnit.isWearable shouldBe None
    testUnit.nothing()
    testUnit.isHarmful.value shouldBe true
    testUnit.isHoldable.value shouldBe true
    testUnit.isIdentifiable.value shouldBe true
    testUnit.isUsable.value shouldBe true
    testUnit.isWearable.value shouldBe true
  }

  "ItemConditions.everything" should "ensure everything is false" in {
    testUnit.isHarmful shouldBe None
    testUnit.isHoldable shouldBe None
    testUnit.isIdentifiable shouldBe None
    testUnit.isUsable shouldBe None
    testUnit.isWearable shouldBe None
    testUnit.everything()
    testUnit.isHarmful.value shouldBe false
    testUnit.isHoldable.value shouldBe false
    testUnit.isIdentifiable.value shouldBe false
    testUnit.isUsable.value shouldBe false
    testUnit.isWearable.value shouldBe false
  }
}
