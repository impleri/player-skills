package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Ingredient

class RecipeTargetSpec extends BaseSpec {
  private val mockRecipeType = mock[ResourceLocation]
  private val outputItem = "minecraft:andesite"
  private val ingredient = "minecraft:stone"
  private val ingredient2 = "minecraft:cobblestone"

  private val testUnit = RecipeTarget(mockRecipeType, Option(outputItem), Seq(ingredient))

  private val testUnitJustOutput = RecipeTarget(mockRecipeType, Option(outputItem))

  private val testUnitJustIngredients = RecipeTarget(mockRecipeType, ingredients = Seq(ingredient, ingredient2))

  private val testUnitEmpty = RecipeTarget(mockRecipeType)

  private val mockRecipe = mock[Recipe[Container]]

  "RecipeTarget.getOutputItem" should "returns the item parsed from the string" in {
    testUnit.getOutputItem.value.name shouldBe outputItem
  }

  "RecipeTarget.getIngredientItems" should "returns the items parsed from the strings" in {
    testUnit.getIngredientItems.head.name shouldBe ingredient
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

    testUnitJustOutput.matches(mockRecipe) shouldBe true
  }

  it should "return false for a recipe that does not have all of the same ingredients" in {
    val ingredients = mock[List[Ingredient]]
    ingredients.contains(*) returns true andThen false
    mockRecipe.getIngredients returns ingredients

    testUnitJustIngredients.matches(mockRecipe) shouldBe false
  }

  it should "return true for a recipe that has all of the same ingredients and no output is targeted" in {
    val ingredients = mock[List[Ingredient]]
    ingredients.contains(*) returns true
    mockRecipe.getIngredients returns ingredients

    testUnitJustIngredients.matches(mockRecipe) shouldBe true
  }

  it should "return true for a recipe that matches the output and has all of the same ingredients" in {
    val recipeOutput = mock[Item]
    recipeOutput.matches(*) returns true
    mockRecipe.getResultItem returns recipeOutput

    val ingredients = mock[List[Ingredient]]
    ingredients.contains(*) returns true
    mockRecipe.getIngredients returns ingredients

    testUnitJustIngredients.matches(mockRecipe) shouldBe true
  }
}
