package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.core.{ResourceLocation => ResourceFacade}
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.crafting.RecipeManager
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SmeltingRecipe
import net.minecraft.world.Container

class RecipeRestrictionBuilderSpec extends BaseSpec {
  private val mockServerState = mock[ServerStateContainer]
  private val mockRegistry = mock[Registry[RecipeType[_]]]
  private val mockRestrictions = mock[RestrictionRegistry]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = RecipeRestrictionBuilder(mockServerState, mockRestrictions, mockRegistry, mockLogger)

  private val mockRecipeTarget = mock[RecipeTarget]
  private val mockServer = mock[Server]
  private val mockManager = mock[RecipeManager]

  private case class TestConditions() extends RecipeConditions {
    targets = Seq(mockRecipeTarget)

    override def name: ResourceLocation = new ResourceLocation("skillstest", "condition")
  }

  private val testBuilder = TestConditions()

  private val mockSmeltingRecipe = mock[Recipe[Container]]

  "RecipeRestrictionBuilder.add" should "adds a new value to the internal restrictions map" in {
    testUnit.restrictions.isEmpty shouldBe true

    testUnit.add(testBuilder)

    testUnit.restrictions.isEmpty shouldBe false
    testUnit.restrictions.values.toList.contains(testBuilder) shouldBe true
  }

  "RecipeRestrictionBuilder.restrict" should "restrict a simple item" in {
    val targetName = "skillstest"

    val mockRecipeType = mock[RecipeType[SmeltingRecipe]]
    val targetRecipeType = ResourceFacade("skillstest", "recipe_type").get
    mockRecipeTarget.recipeType returns targetRecipeType
    mockRegistry.get(targetRecipeType) returns Option(mockRecipeType)

    mockServerState.SERVER returns Option(mockServer)
    mockServer.getRecipeManager returns mockManager
    mockManager.getAllFor[Container, SmeltingRecipe](mockRecipeType) returns Seq(mockSmeltingRecipe)
    mockRecipeTarget.matches(mockSmeltingRecipe) returns true

    val mockItem = mock[Item]
    mockSmeltingRecipe.getName returns None
    mockSmeltingRecipe.getResultItem returns mockItem
    mockItem.name returns "test-recipe"

    testUnit.restrict((targetName, testBuilder))

    mockRestrictions.add(any[RecipeRestriction]) wasCalled once
  }
}
