package net.impleri.playerskills.integrations.jei

import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.integrations.jei.facades.JeiRuntime
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.client.EventHandler
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.slab.client.events.RecipeEvents
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger

import scala.util.chaining.scalaUtilChainingOps

case class JeiPluginState(
  runtime: Option[JeiRuntime] = None,
  hiddenRecipes: Seq[Recipe.Any] = Seq.empty,
) {
  def execute(f: JeiRuntime => Unit): Unit = runtime.foreach(f)
}

case class JeiHelper(
  restrictionRegistry: RestrictionRegistry = PlayerSkills.STATE.RESTRICTIONS,
  recipeEvents: RecipeEvents = RecipeEvents(),
  clientEventHandler: EventHandler = PlayerSkillsClient.EVENTS,
  logger: Logger = PlayerSkillsLogger.ITEMS,
) {
  private var state: JeiPluginState = JeiPluginState()

  private def upsert(next: JeiPluginState): Unit = state = next

  private[jei] def updateRuntime(runtime: Option[JeiRuntime]): Unit =
    state
      .copy(runtime = runtime)
      .pipe(upsert)
      .tap(_ => refresh(true))

  private def refreshHiddenRecipes(
    nextHidden: Seq[Recipe.Any],
    forced: Boolean,
  )(jeiRuntime: JeiRuntime): Unit = {
    val current = state.hiddenRecipes

    val toShow = if (forced) current else nextHidden.diff(current)
    toShow.groupBy(_.getType.toString).pipe(jeiRuntime.showRecipes)

    val toHide = if (forced) nextHidden else current.diff(nextHidden)
    toHide.groupBy(_.getType.toString).pipe(jeiRuntime.hideRecipes)
  }

  def refresh(forced: Boolean = false): Unit =
    restrictionRegistry.entries
      .filter(_.isType(RestrictionType.Recipe))
      .asInstanceOf[List[RecipeRestriction]]
      .map(_.target)
      .tap(n => state.execute(refreshHiddenRecipes(n, forced)))
      .pipe(n => state.copy(hiddenRecipes = n))
      .pipe(upsert)

  clientEventHandler.onSkillsUpdate { event => refresh(event.forced) }
  recipeEvents.onUpdate { _ => refresh() }
}
