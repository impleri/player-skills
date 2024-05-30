package net.impleri.slab.item.crafting

import net.impleri.slab.server.Server
import net.minecraft.world.inventory.{CraftingContainer => McCraftingContainer}

case class CraftingContainer(private val underlying: McCraftingContainer) {
  def getCraftingRecipe(server: Server): Option[Recipe[McCraftingContainer]] = {
    server.getRecipeManager.getRecipeFor(RecipeType.CRAFTING, underlying, server)
  }
}

object CraftingContainer {
  def fromVanilla(underlying: McCraftingContainer): Option[CraftingContainer] = {
    Option(underlying).map(CraftingContainer(_))
  }
}
