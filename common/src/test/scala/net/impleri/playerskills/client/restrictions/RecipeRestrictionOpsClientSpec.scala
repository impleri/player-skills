package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.{Player => PlayerFacade}
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.player.LocalPlayer

class RecipeRestrictionOpsClientSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockClient = mock[Client]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = RecipeRestrictionOpsClient(mockRegistry, mockClient, mockLogger)

  private val mockPlayer = mock[PlayerFacade[LocalPlayer]]
  private val mockEntity = mock[Entity[LocalPlayer]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockTarget = mock[Recipe[_]]

  private val testRestriction = RecipeRestriction(mockTarget)

  mockClient.getPlayer returns mockPlayer

  mockEntity.asPlayer[LocalPlayer] returns mockPlayer

  mockPlayer.asOption returns Option(mockEntity)
  mockPlayer.dimension returns None
  mockPlayer.biomeAt(None) returns None

  mockTarget.getName returns Option(mockTargetName)

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
