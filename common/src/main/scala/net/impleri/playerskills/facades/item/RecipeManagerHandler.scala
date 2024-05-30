package net.impleri.playerskills.facades.item

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.slab.item.crafting.Recipe

import java.util.{List => JavaList}
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object RecipeManagerHandler {
  private def handleRecipeCheck(recipe: Recipe.Any): Boolean = {
    PlayerSkillsClient.STATE.RECIPE_RESTRICTIONS.isProducible(recipe, None)
  }

  def handleOnGetRecipe[T <: Recipe.BaseContainer](value: Option[Recipe[T]]): Boolean = {
    value.fold(RestrictionsOps.DEFAULT_RESPONSE)(handleRecipeCheck)
  }

  def handleOnGetRecipes[T <: Recipe.BaseContainer](value: Seq[Recipe[T]]): JavaList[Recipe.Vanilla[T]] = {
    value.filter(handleRecipeCheck)
      .map(_.value)
      .pipe(_.asJava)
  }
}
