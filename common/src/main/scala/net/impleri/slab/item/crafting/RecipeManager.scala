package net.impleri.slab.item.crafting

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.server.Server
import net.minecraft.world.item.crafting.{RecipeManager => McManager}

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

case class RecipeManager(override val underlying: RecipeManager.Vanilla)
    extends ResourceWrapper[RecipeManager.Vanilla] {
  override val name: Option[ResourceLocation] = None

  def find(key: ResourceLocation): Option[Recipe.Any] =
    underlying
      .byKey(key.value)
      .toScala
      .map(Recipe(_))

  def getRecipeFor[C <: Recipe.BaseContainer, T <: Recipe.Vanilla[C]](
    recipeType: RecipeType.Any,
    container: C,
    server: Server,
  ): Option[Recipe[T]] =
    server.getLevel
      .map(underlying.getRecipeFor[C, T](recipeType.asType[T], container, _))
      .flatMap(_.toScala)
      .map(Recipe(_))

  def getAllFor[T <: Recipe.BaseVanilla](
    recipeType: RecipeType.Any,
  ): Seq[Recipe.Any] =
    underlying
      .getAllRecipesFor[Recipe.BaseContainer, T](recipeType.asType[T])
      .asScala
      .toList
      .map(Recipe(_))
}

object RecipeManager {
  type Vanilla = McManager
}
