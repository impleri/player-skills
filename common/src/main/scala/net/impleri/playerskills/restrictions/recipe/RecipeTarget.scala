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
  private[recipe] lazy val getOutputItem: Option[Item] = output.flatMap(Item.parse)

  private[recipe] lazy val getIngredientItems: Seq[Item] = ingredients.flatMap(Item.parse)

  def matches[C <: Container](recipe: Recipe[C]): Boolean = {
    (getOutputItem.nonEmpty || getIngredientItems.nonEmpty) &&
      getOutputItem.forall(recipe.getResultItem.matches) &&
      getIngredientItems.forall(recipe.getIngredients.contains)
  }
}
