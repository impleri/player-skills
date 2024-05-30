package net.impleri.slab.item.crafting

import net.impleri.slab.server.Server
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.item.crafting.{RecipeManager => McManager}
import net.minecraft.world.Container

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

case class RecipeManager(private val underlying: McManager) {
  def getRecipeFor[C <: Container, T <: McRecipe[C]](
    recipeType: RecipeType[C, T],
    container: C,
    server: Server,
  ): Option[Recipe[C]] = {
    server.getLevel.map(underlying.getRecipeFor(recipeType.value, container, _))
      .flatMap(_.toScala)
      .map(Recipe(_))
  }

  def getAllFor[C <: Container, T <: McRecipe[C]](recipeType: RecipeType[C, T]): Seq[Recipe[C]] = {
    underlying.getAllRecipesFor(recipeType.value)
      .asScala
      .toList
      .map(Recipe(_))
  }
}
