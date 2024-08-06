package net.impleri.slab.item.crafting

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.server.Server
import net.minecraft.world.inventory.{CraftingContainer => McCraftingContainer}

case class CraftingContainer(override val underlying: CraftingContainer.Vanilla)
    extends ResourceWrapper[CraftingContainer.Vanilla] {
  override val name: Option[ResourceLocation] = None

  def getCraftingRecipe(server: Server): Option[Recipe.Any] = {
    server.getRecipeManager
      .getRecipeFor[CraftingContainer.Vanilla, Recipe.Vanilla[
        CraftingContainer.Vanilla,
      ]](
        RecipeType.CRAFTING,
        underlying,
        server,
      )
      .asInstanceOf[Option[Recipe.Any]]
  }
}

object CraftingContainer {
  type Vanilla = McCraftingContainer

  def fromVanilla(underlying: Vanilla): Option[CraftingContainer] = {
    Option(underlying).map(CraftingContainer(_))
  }
}
