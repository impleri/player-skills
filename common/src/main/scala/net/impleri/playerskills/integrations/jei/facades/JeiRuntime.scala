package net.impleri.playerskills.integrations.jei.facades

import mezz.jei.api.recipe.IRecipeManager
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.world.Container

import java.util
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

case class JeiRuntime(private val runtime: IJeiRuntime) {
  def recipeManager: IRecipeManager = {
    runtime.getRecipeManager
  }

  def getType(value: Option[ResourceLocation]): Option[RecipeType[_]] =
    value
      .map(_.value)
      .map(recipeManager.getRecipeType(_))
      .flatMap(_.toScala)

  private def processRecipes[C <: Container, T <: Recipe.Vanilla[C]](
    recipesByType: Map[Option[ResourceLocation], Seq[Recipe.Any]],
    f: (RecipeType[T], util.Collection[T]) => Unit
  ): Unit =
    for {
      (typeName, recipes) <- recipesByType
      _ = PlayerSkillsLogger.RECIPES.info(s"Processing ${recipes.size} JEI recipes for $typeName")
      recipeType <- getType(typeName)
      recipeValues = recipes.map(_.value).asJavaCollection
    } yield f(recipeType.asInstanceOf[RecipeType[T]], recipeValues.asInstanceOf[util.Collection[T]])


  def hideRecipes[C <: Container, T <: Recipe.Vanilla[C]](
    recipesByType: Map[Option[ResourceLocation], Seq[Recipe.Any]],
  ): Unit = processRecipes[C, T](recipesByType, recipeManager.hideRecipes[T])

  def showRecipes[C <: Container, T <: Recipe.Vanilla[C]](
    recipesByType: Map[Option[ResourceLocation], Seq[Recipe.Any]],
  ): Unit = processRecipes[C, T](recipesByType, recipeManager.unhideRecipes[T])
}
