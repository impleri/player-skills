package net.impleri.slab.item.crafting

import com.mojang.datafixers.util.Pair
import net.impleri.slab.item.Item
import net.impleri.slab.registry.HasName
import net.impleri.slab.registry.IsRegistered
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.resources.{ResourceLocation => McResourceLocation}
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType

import java.util.{List => JavaList}
import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.chaining.scalaUtilChainingOps

case class Recipe[C <: Container](private val underlying: McRecipe[C])
  extends IsRegistered[McRecipe[C]] with IsRecipe with HasName {
  def asGeneric: Recipe.Any = this

  val value: McRecipe[C] = underlying

  def getRaw: McRecipe[C] = value

  def getType: RecipeType[McRecipe[C]] = underlying.getType.asInstanceOf[RecipeType[McRecipe[C]]]

  def getResult: ItemStack = underlying.getResultItem

  def getResultItem: Item = getResult.pipe(Item(_))

  def getIngredients: List[Ingredient] = underlying.getIngredients.asScala.toList

  override def getName: Option[ResourceLocation] = Option(underlying.getId).flatMap(ResourceLocation(_))
}

object Recipe {
  type Any = Recipe[_]
  type AnyVanilla = McRecipe[_]
  type Base = Recipe[Container]
  type BaseVanilla = McRecipe[Container]
  type BaseContainer = Container
  type Vanilla[T <: Container] = McRecipe[T]

  def fromVanillaOpt[C <: Container, T <: McRecipe[C]](underlying: Optional[T]): Option[Recipe[C]] = {
    underlying.toScala.map(Recipe(_))
  }

  def fromVanilla[C <: Container](underlying: McRecipe[C]): Option[Recipe[C]] = {
    Option(underlying).map(Recipe(_))
  }

  def fromVanillaPair[C <: Container, T <: McRecipe[C]](value: Optional[Pair[McResourceLocation, T]]): Option[Recipe[C]] = {
    value.toScala.map(_.getSecond).map(Recipe(_))
  }

  def fromVanillaList[C <: Container, T <: McRecipe[C]](values: JavaList[T]): Seq[Recipe[C]] = {
    values.asScala.flatMap(fromVanilla).toSeq
  }
}
