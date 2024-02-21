package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

class RecipeRestrictionOps(
  protected val registry: RestrictionRegistry,
  protected val logger: PlayerSkillsLogger,
)
  extends RestrictionsOps[Recipe[_], RecipeRestriction] {
  override val restrictionType: RestrictionType = RestrictionType.Recipe()

  def isProducible(player: Player[_], recipe: Recipe[_], pos: Option[Position] = None): Boolean = {
    canPlayer(player, recipe, _.producible, "producible", pos)
  }
}

object RecipeRestrictionOps {
  def apply(
    registry: RestrictionRegistry = RestrictionRegistry(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  ): RecipeRestrictionOps = {
    new RecipeRestrictionOps(registry, logger)
  }
}
