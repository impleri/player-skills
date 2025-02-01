package net.impleri.slab.item.crafting

import net.impleri.slab.item.Item

import scala.util.chaining.scalaUtilChainingOps

trait IsRecipe {
  def getResult: Item.VanillaStack

  def getResultItem: Item = getResult.pipe(Item(_))

  def getIngredients: List[Item.VanillaIngredient]

  def getIngredientItems: List[Item] =
    getIngredients.flatMap(_.getItems).map(Item(_))
}
