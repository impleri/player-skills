package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.client.Client
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Position

case class RecipeRestrictionOpsClient(
  r: RestrictionRegistry = RestrictionRegistry(),
  protected val client: Client = Client(),
  l: Logger = PlayerSkillsLogger.ITEMS,
) extends RecipeRestrictionOps(r, l) with RestrictionOpsClient {
  def isProducible(recipe: Recipe[_], pos: Option[Position]): Boolean = {
    maybeCan(isProducible(_, recipe, pos))
  }
}
