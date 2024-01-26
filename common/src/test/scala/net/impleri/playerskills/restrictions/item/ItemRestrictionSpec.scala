package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.resources.ResourceLocation

class ItemRestrictionSpec extends BaseSpec {
  private val mockTarget = mock[Item]

  private case class TestConditions() extends ItemConditions {
    override def name: ResourceLocation = new ResourceLocation("skillstest", "condition")
  }

  "ItemRestriction.apply" should "create an ItemRestriction" in {
    val testUnit = ItemRestriction(mockTarget)
    testUnit.target shouldBe mockTarget
  }

  it should "create an ItemRestriction from a builder" in {
    val conditions = TestConditions()
    val testUnit = ItemRestriction(mockTarget, conditions)
    testUnit.target shouldBe mockTarget
  }
}
