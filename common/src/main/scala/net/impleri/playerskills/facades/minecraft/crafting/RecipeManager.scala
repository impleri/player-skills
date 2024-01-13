package net.impleri.playerskills.facades.minecraft.crafting

import com.mojang.datafixers.util.Pair
import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.facades.minecraft.Server
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.item.crafting.{RecipeManager => McManager}
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.Container

import java.util.{List => JavaList}
import java.util.Optional
import scala.jdk.javaapi.CollectionConverters
import scala.jdk.javaapi.OptionConverters
import scala.util.chaining.scalaUtilChainingOps

case class RecipeManager(recipeManager: McManager) {
  def getRecipeFor[C <: Container, T <: McRecipe[C]](
    recipeType: RecipeType[T],
    container: C,
    server: Server,
  ): Option[Recipe[C]] = {
    server.getLevel.map(recipeManager.getRecipeFor(recipeType, container, _))
      .flatMap(OptionConverters.toScala[T])
      .map(Recipe[C])
  }
}

object RecipeManager {
  private def handleRecipeCheck[C <: Container, T <: McRecipe[C]](recipe: T) = {
    Recipe(recipe)
      .pipe(PlayerSkillsClient.STATE.RECIPE_RESTRICTIONS.isProducible(_, None))
  }

  def handleOnGetRecipe[C <: Container, T <: McRecipe[C]](value: Optional[T]): Boolean = {
    val valueOpt = OptionConverters.toScala(value)

    if (valueOpt.isEmpty) {
      true
    } else {
      valueOpt.fold(RestrictionsOps.DEFAULT_RESPONSE)(handleRecipeCheck[C, T])
    }
  }

  def handleOnGetRecipePair[C <: Container, T <: McRecipe[C]](value: Optional[Pair[ResourceLocation, T]]): Boolean = {
    val valueOpt = OptionConverters.toScala(value)

    if (valueOpt.isEmpty) {
      true
    } else {
      valueOpt.map(_.getSecond).fold(RestrictionsOps.DEFAULT_RESPONSE)(handleRecipeCheck[C, T])
    }
  }

  def handleOnGetRecipes[C <: Container, T <: McRecipe[C]](value: JavaList[T]): JavaList[T] = {
    CollectionConverters.asScala(value)
      .toList
      .filter(handleRecipeCheck[C, T])
      .pipe(CollectionConverters.asJava(_))
  }
}
