package net.impleri.playerskills.facades.minecraft.crafting

import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Ingredient

import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

case class Recipe[C <: Container](private val data: McRecipe[C]) extends HasName {
  def getIngredients: List[Ingredient] = CollectionConverters.asScala(data.getIngredients).toList

  def getIngredientItems: List[Item] = getIngredients.flatMap(_.getItems).map(Item(_))

  def getOutput: Item = {
    data.getResultItem
      .pipe(i => Item(i))
  }

  override def getName: Option[ResourceLocation] = Option(data.getId).map(ResourceLocation(_))
}
