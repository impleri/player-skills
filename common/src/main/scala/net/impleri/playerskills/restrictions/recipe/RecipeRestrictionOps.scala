package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Position

class RecipeRestrictionOps(
  protected val registry: RestrictionRegistry,
  protected val logger: Logger,
)
  extends RestrictionsOps[Recipe.Any, Recipe.AnyVanilla, RecipeRestriction] {
  override val restrictionType: RestrictionType = RestrictionType.Recipe()

  def isProducible(player: Player.Any, recipe: Recipe.Any, pos: Option[Position] = None): Boolean = {
    canPlayer(player, recipe, _.producible, "producible", pos)
  }
}

object RecipeRestrictionOps {
  def apply(
    registry: RestrictionRegistry = RestrictionRegistry(),
    logger: Logger = PlayerSkillsLogger.ITEMS,
  ): RecipeRestrictionOps = {
    new RecipeRestrictionOps(registry, logger)
  }
}
