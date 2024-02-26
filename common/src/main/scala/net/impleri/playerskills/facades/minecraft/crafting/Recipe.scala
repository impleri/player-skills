package net.impleri.playerskills.facades.minecraft.crafting

import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait IsRecipe {
  def getResult: ItemStack

  def getResultItem: Item

  def getIngredients: List[Ingredient]

  def getIngredientItems: List[Item] = getIngredients.flatMap(_.getItems).map(Item(_))

  def getOutput: Item = getResultItem
}

case class Recipe[C <: Container](private val data: McRecipe[C]) extends IsRecipe with HasName {
  def getRaw: McRecipe[C] = data

  def getType: RecipeType[McRecipe[C]] = data.getType.asInstanceOf[RecipeType[McRecipe[C]]]

  def getResult: ItemStack = data.getResultItem

  def getResultItem: Item = getResult.pipe(Item(_))

  def getIngredients: List[Ingredient] = data.getIngredients.asScala.toList

  override def getName: Option[ResourceLocation] = Option(data.getId).map(ResourceLocation(_))
}
