package net.impleri.slab.item.crafting

import net.impleri.slab.item.Item

trait IsRecipe {
  def getResult: Item.VanillaStack

  def getResultItem: Item

  def getIngredients: List[Item.VanillaIngredient]

  def getIngredientItems: List[Item] =
    getIngredients.flatMap(_.getItems).map(Item(_))
}
