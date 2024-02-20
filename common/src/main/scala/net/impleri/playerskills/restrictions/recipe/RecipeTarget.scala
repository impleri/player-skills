package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.world.Container

case class RecipeTarget(
  recipeType: ResourceLocation,
  output: Option[String] = None,
  ingredients: Seq[String] = Seq.empty,
) {
  private lazy val getOutputItem: Option[Item] = output.flatMap(Item.parse)

  private lazy val getIngredientItems: Seq[Item] = ingredients.flatMap(Item.parse)

  def matches[C <: Container](recipe: Recipe[C]): Boolean = {
    getOutputItem.exists(recipe.getResultItem.matches) && getIngredientItems.forall(recipe.getIngredients.contains)
  }
}
