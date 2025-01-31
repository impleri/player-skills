package net.impleri.playerskills.integrations.rei

import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.Display
import net.impleri.playerskills.api.restrictions.{Restriction, RestrictionType}
import net.impleri.playerskills.client.restrictions.RecipeRestrictionOpsClient
import net.impleri.playerskills.integrations.rei.facades.BrewingRecipe
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.EventHandler
import net.impleri.slab.item.crafting.IsRecipe
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger

import scala.collection.View

case class RecipeRestrictionsPredicate(
  displayRegistry: DisplayRegistry,
  restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  recipeOpsClient: RecipeRestrictionOpsClient = RecipeRestrictionOpsClient(),
  logger: Logger = PlayerSkillsLogger.RECIPES,
) extends DisplayVisibilityPredicate with EventHandler {
  override def getPriority: Double = 100

  override def handleDisplay(
    category: DisplayCategory[_],
    display: Display,
  ) =
    failOn {
      castDisplayToRecipe(display)
        .map(isRecipeAllowed)
    }

  private def getRestrictedRecipes: View[Recipe[_]] =
    restrictionRegistry.entries.view
      .filter(_.isType(RestrictionType.Recipe))
      .asInstanceOf[View[RecipeRestriction]]
      .map(_.target)
      .filterNot(recipeOpsClient.isProducible(_, None))

  private def isRecipeAllowed(recipe: IsRecipe): Boolean =
    recipe match {
      case r: Recipe[_] => {
        val isRestricted = getRestrictedRecipes.exists(_ == r)

        if (isRestricted) {
          logger.info(s"Restricted REI from showing $r")
        }

        // Invert so the isRestricted means is not allowed
        !isRestricted
      }
      // TODO: Handle brewing
      case _ => Restriction.DEFAULT_RESPONSE
    }

  private def castDisplayToRecipe(value: Display): Option[IsRecipe] =
    Option(displayRegistry.getDisplayOrigin(value)) flatMap {
      case r: Recipe.AnyVanilla => Option(Recipe(r))
      case b: BrewingRecipe.Vanilla => Option(BrewingRecipe(b))
      case _               => None
    }
}
