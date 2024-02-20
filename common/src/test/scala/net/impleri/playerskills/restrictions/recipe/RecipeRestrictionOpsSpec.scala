package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.{Player => PlayerFacade}
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.player.Player

class RecipeRestrictionOpsSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = RecipeRestrictionOps(mockRegistry, mockLogger)

  private val mockPlayer = mock[PlayerFacade[Player]]
  private val mockEntity = mock[Entity[Player]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockTarget = mock[Recipe[_]]

  private val testRestriction = RecipeRestriction(mockTarget)

  mockEntity.asPlayer[Player] returns mockPlayer

  mockPlayer.asOption returns Option(mockEntity)
  mockPlayer.dimension returns None
  mockPlayer.biomeAt(None) returns None

  mockTarget.getName returns Option(mockTargetName)

  "RecipeRestrictionOps.isProducible" should "return false if a restriction has identifiable = false" in {
    val testValue = false
    mockRegistry.entries returns List(testRestriction.copy(producible = testValue))
    testUnit.isProducible(mockPlayer, mockTarget) shouldBe testValue
  }

  it should "return true if no restrictions have identifiable = false" in {
    mockRegistry.entries returns List.empty
    testUnit.isProducible(mockPlayer, mockTarget) shouldBe true
  }
}
