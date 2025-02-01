package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.api.restrictions.TargetResource
import net.impleri.playerskills.utils.SeqExtensions.EveryFn
import net.impleri.slab.item.Item
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.registry.IsIngredient
import net.impleri.slab.registry.ItemTag
import net.impleri.slab.registry.ResourceNamespace
import net.impleri.slab.resources.ResourceLocation

case class RecipeTarget(
  recipeType: ResourceLocation,
  output: Option[String] = None,
  ingredients: Seq[String] = Seq.empty,
  allowOtherIngredients: Boolean = true,
) {
  override def toString: String = {
    val outputString = getOutputItem.fold("")(v => s"[$v]")
    val inputs = getIngredients.mkString("{", ", ", "}")
    s"$recipeType$outputString$inputs"
  }
  private[recipe] def castRecipeContents(
    input: Seq[String],
  ): Seq[IsIngredient] = {
    for {
      resource <- input
      target <- TargetResource.create(resource, singleAsString = true)
      ingredient <- RecipeTarget.castTargetResource(target)
    } yield ingredient
  }

  private[recipe] lazy val getOutputItem: Option[IsIngredient] =
    castRecipeContents(output.toList)
      .headOption

  private[recipe] lazy val getIngredients: Seq[IsIngredient] =
    castRecipeContents(ingredients)

  private def matchesIngredients(recipeIngredients: Seq[Item]): Boolean = {
    val ingredients = getIngredients
    val usesAllIngredients = ingredients.every(_.inList(recipeIngredients))
    if (allowOtherIngredients) usesAllIngredients
    else recipeIngredients.every(i => ingredients.exists(_.inList(Seq(i))))
  }

  def matches(recipe: Recipe.Any): Boolean = {
    getOutputItem.exists(recipe.getResultItem.matches) &&
    matchesIngredients(recipe.getIngredientItems)
  }
}

object RecipeTarget {
  private def castTargetResource(targetResource: TargetResource): Option[IsIngredient] =
    targetResource match {
      case ns: TargetResource.Namespace =>
        Option(ResourceNamespace(ns.target))
      case s: TargetResource.Single => Item(s.target)
      case s: TargetResource.SingleString => Item.parse(s.target)
      case t: TargetResource.Tag[_, _] => Option(ItemTag(t.target))
      case _ => None
    }
}
