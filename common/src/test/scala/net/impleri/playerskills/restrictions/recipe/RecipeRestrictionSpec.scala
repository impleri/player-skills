package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.resources.ResourceLocation

class RecipeRestrictionSpec extends BaseSpec {
  private val mockTarget = mock[Recipe.Any]

  private case class TestConditions() extends RecipeConditions {
    override def name: ResourceLocation = ResourceLocation("skillstest", "condition").get
  }

  "RecipeRestriction.apply" should "create an RecipeRestriction" in {
    val testUnit = RecipeRestriction(mockTarget)
    testUnit.target shouldBe mockTarget
  }

  it should "create an RecipeRestriction from a builder" in {
    val conditions = TestConditions()
    val testUnit = RecipeRestriction(mockTarget, conditions)
    testUnit.target shouldBe mockTarget
  }
}
