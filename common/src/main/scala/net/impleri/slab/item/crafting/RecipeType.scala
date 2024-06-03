package net.impleri.slab.item.crafting

import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.item.crafting.{RecipeType => McRecipeType}

case class RecipeType[T <: RecipeType.AnyVanilla](override val underlying: T)
  extends ResourceWrapper[T] {
  override val name: Option[ResourceLocation] = Registry
    .RecipeTypes
    .getKey(this.asInstanceOf[RecipeType[RecipeType.AnyVanilla]])

  def asType[R <: Recipe.AnyVanilla]: RecipeType.Vanilla[R] = value.asInstanceOf[RecipeType.Vanilla[R]]
}

object RecipeType {
  type Vanilla[T <: Recipe.AnyVanilla] = McRecipeType[T]
  type AnyVanilla = Vanilla[_]

  type Any = RecipeType[_]

  val CRAFTING: Any = RecipeType(McRecipeType.CRAFTING)

  val SMELTING: Any = RecipeType(McRecipeType.SMELTING)
}
