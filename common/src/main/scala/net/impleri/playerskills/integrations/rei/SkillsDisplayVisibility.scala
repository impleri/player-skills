package net.impleri.playerskills.integrations.rei

import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.plugin.common.displays.brewing.{BrewingRecipe => RawBrewing}
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.client.restrictions.RecipeRestrictionOpsClient
import net.impleri.playerskills.integrations.rei.facades.BrewingRecipe
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.EventHandler
import net.impleri.slab.item.crafting.IsRecipe
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger
import net.minecraft.world.item.crafting.{Recipe => RawRecipe}

import scala.collection.SeqView
import scala.collection.View
import scala.util.chaining.scalaUtilChainingOps

case class SkillsDisplayVisibility(
  displayRegistry: DisplayRegistry,
  restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  recipeOpsClient: RecipeRestrictionOpsClient = RecipeRestrictionOpsClient(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends DisplayVisibilityPredicate
    with EventHandler {
  override def getPriority: Double = 100

  override def handleDisplay(
    category: DisplayCategory[_],
    display: Display,
  ) =
    failOn {
      castDisplayToRecipe(display)
        .fold(false)(hasMatchingRestriction)
        .pipe(Option(_))
    }

  private def getRestrictedRecipes: View[Recipe[_]] =
    restrictionRegistry.entries.view
      .filter(_.isType(RestrictionType.Recipe))
      .asInstanceOf[SeqView[RecipeRestriction]]
      .map(_.target)
      .filter(recipeOpsClient.isProducible(_, None))

  private def hasMatchingRestriction(recipe: IsRecipe): Boolean =
    recipe match {
      case r: Recipe[_] => getRestrictedRecipes.exists(_ == r)
      // TODO: Handle brewing
      case _ => false
    }

  private def castDisplayToRecipe(value: Display): Option[IsRecipe] =
    Option(displayRegistry.getDisplayOrigin(value)) flatMap {
      case r: RawRecipe[_] => Option(Recipe(r))
      case b: RawBrewing   => Option(BrewingRecipe(b))
      case _               => None
    }
}
