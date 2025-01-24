package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.item.crafting.RecipeManager
import net.impleri.slab.item.crafting.RecipeType
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.server.Server
import net.minecraft.world.item.crafting.SmeltingRecipe

class RecipeRestrictionBuilderSpec extends BaseSpec {
  private val mockServerState = mock[ServerStateContainer]
  private val mockRegistry = mock[Registry.RECIPE_TYPE]
  private val mockRestrictions = mock[RestrictionRegistry]
  private val mockLogger = mock[Logger]

  private val testUnit = RecipeRestrictionBuilder(mockServerState, mockRestrictions, mockRegistry, mockLogger)

  private val mockRecipeTarget = mock[RecipeTarget]
  private val mockServer = mock[Server]
  private val mockManager = mock[RecipeManager]

  private case class TestConditions() extends RecipeConditions {
    targets = Seq(mockRecipeTarget)

    override def name: ResourceLocation = ResourceLocation("skillstest", "condition").get
  }

  private val testBuilder = TestConditions()

  private val mockSmeltingRecipe = mock[Recipe.Any]

  "RecipeRestrictionBuilder.add" should "adds a new value to the internal restrictions map" in {
    testUnit.restrictions.isEmpty shouldBe true

    testUnit.add("test")(testBuilder)

    testUnit.restrictions.isEmpty shouldBe false
    testUnit.restrictions.values.toList.contains(testBuilder) shouldBe true
  }

  "RecipeRestrictionBuilder.restrict" should "restrict a simple item" in {
    val targetName = "skillstest"

    val mockRecipeType = mock[RecipeType.Any]
    val targetRecipeType = ResourceLocation("skillstest", "recipe_type").get
    mockRecipeTarget.recipeType returns targetRecipeType
    mockRegistry.find(targetRecipeType) returns Option(mockRecipeType)

    mockServerState.SERVER returns Option(mockServer)
    mockServer.getRecipeManager returns mockManager
    mockManager.getAllFor[SmeltingRecipe](mockRecipeType) returns Seq(mockSmeltingRecipe)
    mockRecipeTarget.matches(mockSmeltingRecipe) returns true

    val mockItem = mock[Item]
    mockSmeltingRecipe.name returns None
    mockSmeltingRecipe.getResultItem returns mockItem
    mockItem.name returns ResourceLocation("skillstest", "recipe")

    testUnit.restrict((targetName, testBuilder))

    mockRestrictions.add(any[RecipeRestriction]) wasCalled once
  }
}
