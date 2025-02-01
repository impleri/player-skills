package net.impleri.playerskills.facades.recipe

import com.mojang.datafixers.util.Pair
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.facades.GuaranteeResponse
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.resources.ResourceLocation

import java.util.{Optional, List => JavaList}
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object ClientRecipeHandler extends GuaranteeResponse {
  private def checkRecipe(recipe: Recipe.Any): Boolean =
    PlayerSkillsClient.STATE.RECIPE_RESTRICTIONS.isProducible(recipe, None)

  def isRecipeProducible(
    value: Recipe.AnyVanilla,
  ): Boolean =
    guarantee {
      Recipe.fromVanilla(value).map(checkRecipe)
    }

  def handleOnGetRecipe(
    value: Optional[Recipe.AnyVanilla],
  ): Boolean =
    guarantee {
      value
        .toScala
        .flatMap(Recipe.fromVanilla)
        .map(checkRecipe)
    }

  def handleOnGetRecipePair(
    value: Optional[Pair[ResourceLocation.Vanilla, Recipe.AnyVanilla]],
  ): Boolean =
    handleOnGetRecipe(value.map(_.getSecond))

  def handleOnGetRecipes(
    value: JavaList[Recipe.AnyVanilla],
  ): JavaList[Recipe.AnyVanilla] =
    value
      .asScala
      .flatMap(Recipe.fromVanilla)
      .filter(checkRecipe)
      .map(_.underlying)
      .asJava
}
