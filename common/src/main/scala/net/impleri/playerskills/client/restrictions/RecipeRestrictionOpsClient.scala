package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class RecipeRestrictionOpsClient(
  r: RestrictionRegistry = RestrictionRegistry(),
  protected val client: Client = Client(),
  l: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RecipeRestrictionOps(r, l) with RestrictionOpsClient {
  def isProducible(recipe: Recipe[_], pos: Option[Position]): Boolean = {
    isProducible(getPlayer, recipe, pos)
  }
}
