package net.impleri.slab.item.crafting

import com.mojang.datafixers.util.Pair
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.Container

import java.util.{List => JavaList}
import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class Recipe[T <: Recipe.AnyVanilla](override val underlying: T)
  extends ResourceWrapper[T] with IsRecipe {
  override val name: Option[ResourceLocation] = Option(underlying.getId).flatMap(ResourceLocation(_))

  def getType: RecipeType.Any = RecipeType(underlying.getType)

  def getResult: Item.VanillaStack = underlying.getResultItem

  def getResultItem: Item = getResult.pipe(Item(_))

  def getIngredients: List[Item.VanillaIngredient] = underlying.getIngredients.asScala.toList
}

object Recipe {
  type BaseContainer = Container
  type Vanilla[T <: BaseContainer] = McRecipe[T]
  type AnyVanilla = Vanilla[_]
  type BaseVanilla = Vanilla[BaseContainer]

  type Any = Recipe[AnyVanilla]

  def fromVanillaOpt[C <: BaseContainer, T <: Vanilla[C]](underlying: Optional[T]): Option[Recipe[T]] = {
    underlying.toScala.map(Recipe(_))
  }

  def fromVanilla[C <: BaseContainer, T <: Vanilla[C]](underlying: T): Option[Recipe[T]] = {
    Option(underlying).map(Recipe(_))
  }

  def fromVanillaPair[C <: BaseContainer, T <: Vanilla[C]](value: Optional[Pair[ResourceLocation.Vanilla, T]]): Option[Recipe[T]] = {
    value.toScala.map(_.getSecond).map(Recipe(_))
  }

  def fromVanillaList[C <: BaseContainer, T <: Vanilla[C]](values: JavaList[T]): Seq[Recipe[T]] = {
    values.asScala.flatMap(fromVanilla[C, T]).toSeq
  }
}
