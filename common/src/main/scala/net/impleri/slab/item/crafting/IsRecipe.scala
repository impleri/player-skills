package net.impleri.slab.item.crafting

import net.impleri.slab.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.ItemStack

trait IsRecipe {
  def getResult: ItemStack

  def getResultItem: Item

  def getIngredients: List[Ingredient]

  def getIngredientItems: List[Item] = getIngredients.flatMap(_.getItems).map(Item(_))

  def getOutput: Item = getResultItem
}
