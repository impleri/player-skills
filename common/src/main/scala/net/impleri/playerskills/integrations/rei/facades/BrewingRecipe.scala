package net.impleri.playerskills.integrations.rei.facades

import me.shedaniel.rei.plugin.common.displays.brewing.{BrewingRecipe => RawBrew}
import net.impleri.playerskills.facades.minecraft.crafting.IsRecipe
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.ItemStack

case class BrewingRecipe(private val data: RawBrew) extends IsRecipe {
  override def getResult: ItemStack = data.output

  override def getResultItem: Item = Item(getResult)

  override def getIngredients: List[Ingredient] = List(data.input, data.ingredient)
}
