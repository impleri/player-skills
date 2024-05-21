package net.impleri.playerskills.integrations.jei.facades

import mezz.jei.api.recipe.IRecipeManager
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.minecraft.world.Container

import java.util
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

case class JeiRuntime(private var runtime: IJeiRuntime) {
  def recipeManager: IRecipeManager = {
    runtime.getRecipeManager
  }

  def getType(value: String): Option[RecipeType[_]] = {
    ResourceLocation(value, isSkill = false)
      .flatMap(t => recipeManager.getRecipeType(t.name).toScala)
  }

  def hideRecipes[C <: Container, T <: Recipe[C]](recipesByType: Map[String, Seq[Recipe[_]]]): Unit = {
    recipesByType
      .foreach(
        v => getType(v._1)
          .foreach(
            t => recipeManager.hideRecipes[T](
              t.asInstanceOf[RecipeType[T]],
              v._2.map(_.getRaw).asJavaCollection.asInstanceOf[util.Collection[T]],
            ),
          ),
      )
  }

  def showRecipes[C <: Container, T <: Recipe[C]](recipesByType: Map[String, Seq[Recipe[_]]]): Unit = {
    recipesByType
      .foreach(
        v => getType(v._1)
          .foreach(
            t => recipeManager.unhideRecipes[T](
              t.asInstanceOf[RecipeType[T]],
              v._2.map(_.getRaw).asJavaCollection.asInstanceOf[util.Collection[T]],
            ),
          ),
      )
  }
}
