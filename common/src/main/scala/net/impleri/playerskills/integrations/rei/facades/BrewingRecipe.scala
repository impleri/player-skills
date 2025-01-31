package net.impleri.playerskills.integrations.rei.facades

import me.shedaniel.rei.plugin.common.displays.brewing.{BrewingRecipe => RawBrew}
import net.impleri.slab.item.crafting.IsRecipe
import net.impleri.slab.item.Item

case class BrewingRecipe(private val data: RawBrew) extends IsRecipe {
  def getResult = data.output

  def getIngredients = List(data.input, data.ingredient)
}

object BrewingRecipe {
  type Vanilla = RawBrew
}
