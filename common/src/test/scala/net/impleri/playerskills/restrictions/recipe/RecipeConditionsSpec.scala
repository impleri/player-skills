package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.ResourceLocation

class RecipeConditionsSpec extends BaseSpec {
  private case class TestConditions() extends RecipeConditions {
    override def name: ResourceLocation = new ResourceLocation("skillstest", "condition")
  }

  private val testUnit = TestConditions()

  "RecipeConditions.producible" should "ensure isProducible is true" in {
    testUnit.isProducible shouldBe None
    testUnit.producible()
    testUnit.isProducible.value shouldBe true
  }

  "RecipeConditions.unproducible" should "ensure isProducible is false" in {
    testUnit.isProducible shouldBe None
    testUnit.unproducible()
    testUnit.isProducible.value shouldBe false
  }

  "RecipeConditions.nothing" should "ensure everything is true" in {
    testUnit.isProducible shouldBe None
    testUnit.nothing()
    testUnit.isProducible.value shouldBe true
  }

  "RecipeConditions.everything" should "ensure everything is false" in {
    testUnit.isProducible shouldBe None
    testUnit.everything()
    testUnit.isProducible.value shouldBe false
  }
}
