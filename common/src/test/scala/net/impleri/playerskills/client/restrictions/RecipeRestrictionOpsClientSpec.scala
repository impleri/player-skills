package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.slab.client.Client
import net.impleri.slab.client.{Player => PlayerFacade}
import net.impleri.slab.entity.{Entity, Player}
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.client.player.LocalPlayer

class RecipeRestrictionOpsClientSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockClient = mock[Client]
  private val mockLogger = mock[Logger]

  private val testUnit = RecipeRestrictionOpsClient(mockRegistry, mockClient, mockLogger)

  private val mockPlayer = mock[PlayerFacade]
  private val mockEntity = mock[Entity[Player.Vanilla]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockTarget = mock[Recipe.Any]

  private val testRestriction = new RecipeRestriction(mockTarget)

  mockClient.getPlayer returns Option(mockPlayer)

  mockEntity.asPlayer returns mockPlayer

  mockPlayer.asOption returns Option(mockEntity)
  mockPlayer.dimension returns None
  mockPlayer.biomeAt(None) returns None

  mockTarget.name returns Option(mockTargetName)

  "RecipeRestrictionOpsClient.isProducible" should "return false if a restriction has producible = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(producible = testValue))
    testUnit.isProducible(mockTarget, None) shouldBe testValue
  }

  it should "return true if no restrictions have producible = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isProducible(mockTarget, None) shouldBe true
  }
}
