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

  def handleOnGetRecipe[C <: Recipe.BaseContainer, T <: Recipe.Vanilla[C]](value: Option[Recipe[T]]): Boolean = {
    value.fold(RestrictionsOps.DEFAULT_RESPONSE)(r => handleRecipeCheck(r.asInstanceOf[Recipe.Any]))
  }

  def handleOnGetRecipes[C <: Recipe.BaseContainer, T <: Recipe.Vanilla[C]](value: Seq[Recipe[T]]): JavaList[Recipe.AnyVanilla] = {
    value.filter(r => handleRecipeCheck(r.asInstanceOf[Recipe.Any]))
      .map(_.value.asInstanceOf[Recipe.AnyVanilla])
      .pipe(_.asJava)
  }
}
