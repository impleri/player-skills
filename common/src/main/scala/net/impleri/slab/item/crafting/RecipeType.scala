package net.impleri.slab.item.crafting

import net.impleri.slab.registry.IsRegistered
import net.minecraft.world.item.crafting.{RecipeType => McRecipeType}
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.CraftingRecipe

case class RecipeType[C <: Container, T <: Recipe[C]](private val underlying: McRecipeType[T])
  extends IsRegistered[McRecipeType[T]] {
  val value: McRecipeType[T] = underlying
}

object RecipeType {
  type Any = RecipeType[_, _]

  val CRAFTING: RecipeType[_, CraftingRecipe] = RecipeType(McRecipeType.CRAFTING)
}
