package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.api.restrictions.RestrictionTarget
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.IsIngredient
import net.impleri.playerskills.facades.minecraft.ItemTag
import net.impleri.playerskills.facades.minecraft.ResourceNamespace
import net.minecraft.world.item.{Item => McItem}
import net.minecraft.world.Container

case class RecipeTarget(
  recipeType: ResourceLocation,
  output: Option[String] = None,
  ingredients: Seq[String] = Seq.empty,
) {
  private[recipe] def castRecipeContents(input: Seq[String]): Seq[IsIngredient] = {
    input
      .flatMap(RestrictionTarget(_, singleAsString = true))
      .flatMap {
        case ns: RestrictionTarget.Namespace => Option(ResourceNamespace(ns.target))
        case s: RestrictionTarget.Single => Item.parse(s.target.toString)
        case s: RestrictionTarget.SingleString => Item.parse(s.target)
        case t: RestrictionTarget.Tag[McItem] => Option(ItemTag(t.target))
        case _ => None
      }
  }

  private[recipe] lazy val getOutputItem: Option[IsIngredient] = castRecipeContents(output.toList).headOption

  private[recipe] lazy val getIngredients: Seq[IsIngredient] = castRecipeContents(ingredients)

  def matches[C <: Container](recipe: Recipe[C]): Boolean = {
    (getOutputItem.nonEmpty || getIngredients.nonEmpty) &&
      getOutputItem.forall(recipe.getResultItem.matches) &&
      getIngredients.forall(_.inList(recipe.getIngredientItems))
  }
}
