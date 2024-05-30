package net.impleri.playerskills.integrations.rei.facades

import me.shedaniel.rei.plugin.common.displays.brewing.{BrewingRecipe => RawBrew}
import net.impleri.slab.item.crafting.IsRecipe
import net.impleri.slab.item.Item

case class BrewingRecipe(private val data: RawBrew) extends IsRecipe {
  override def getResult = data.output

  override def getResultItem = Item(getResult)

  override def getIngredients = List(data.input, data.ingredient)
}
