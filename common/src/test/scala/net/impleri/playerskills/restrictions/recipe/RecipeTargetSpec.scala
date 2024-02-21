package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.Container

class RecipeTargetSpec extends BaseSpec {
  private val mockRecipeType = mock[ResourceLocation]
  private val outputItem = "minecraft:andesite"
  private val ingredient = "minecraft:stone"
  private val ingredient2 = "#stone_bricks"

  private val testUnit = RecipeTarget(mockRecipeType, Option(outputItem), Seq(ingredient, ingredient2))

  private val testUnitJustOutput = RecipeTarget(mockRecipeType, Option(outputItem), Seq.empty)

  private val testUnitJustIngredients = RecipeTarget(mockRecipeType, ingredients = Seq(ingredient, ingredient2))

  private val testUnitEmpty = RecipeTarget(mockRecipeType)

  private val mockRecipe = mock[Recipe[Container]]

  "RecipeTarget.getOutputItem" should "returns the item parsed from the string" in {
    testUnit.getOutputItem.value.isInstanceOf[Item] shouldBe true
    testUnit.getOutputItem.value.asInstanceOf[Item].name shouldBe outputItem
  }

  "RecipeTarget.getIngredientItems" should "returns the items parsed from the strings" in {
    testUnit.getIngredients.head.isInstanceOf[Item] shouldBe true
    testUnit.getIngredients.head.asInstanceOf[Item].name shouldBe ingredient
  }

  "RecipeTarget.matches" should "return false if target has no output and no ingredients" in {
    testUnitEmpty.matches(mockRecipe) shouldBe false
  }

  it should "return false for a recipe that does not have the same output" in {
    val recipeOutput = mock[Item]
    recipeOutput.matches(*) returns false
    mockRecipe.getResultItem returns recipeOutput

    testUnitJustOutput.matches(mockRecipe) shouldBe false
  }

  it should "return true for a recipe that has the same output and no ingredients are targeted" in {
    val recipeOutput = mock[Item]
    recipeOutput.matches(*) returns true
    mockRecipe.getResultItem returns recipeOutput

    val result = testUnitJustOutput.matches(mockRecipe) shouldBe true
  }

  it should "return false for a recipe that does not have all of the same ingredients" in {
    val ingredients = mock[List[Item]]
    ingredients.exists(*) returns false
    mockRecipe.getIngredientItems returns ingredients

    testUnitJustIngredients.matches(mockRecipe) shouldBe false
  }

  it should "return true for a recipe that has all of the same ingredients and no output is targeted" in {
    val ingredients = mock[List[Item]]
    ingredients.exists(*) returns true
    mockRecipe.getIngredientItems returns ingredients

    testUnitJustIngredients.matches(mockRecipe) shouldBe true
  }

  it should "return true for a recipe that matches the output and has all of the same ingredients" in {
    val recipeOutput = mock[Item]
    recipeOutput.matches(*) returns true
    mockRecipe.getResultItem returns recipeOutput

    val ingredients = mock[List[Item]]
    ingredients.exists(*) returns true
    mockRecipe.getIngredientItems returns ingredients

    testUnit.matches(mockRecipe) shouldBe true
  }
}
