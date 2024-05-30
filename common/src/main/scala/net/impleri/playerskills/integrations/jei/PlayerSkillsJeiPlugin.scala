package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.IModPlugin
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.client.EventHandler
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.integrations.jei.facades.JeiRuntime
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class JeiPluginState(runtime: Option[JeiRuntime] = None, hiddenRecipes: Seq[Recipe[_]] = Seq.empty) {
  def execute(f: JeiRuntime => Unit): Unit = runtime.foreach(f)
}

@REIPluginCompatIgnore
case class PlayerSkillsJeiPlugin(
  restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  clientEventHandler: EventHandler = PlayerSkillsClient.EVENTS,
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends IModPlugin {
  private var state: JeiPluginState = JeiPluginState()

  private def upsert(next: JeiPluginState): Unit = state = next

  def registerEvents(): Unit = {
    clientEventHandler.onSkillsUpdate { event => refresh(event.forced) }
  }

  override def getPluginUid = ResourceLocation(PlayerSkills.MOD_ID, "jei_plugin").get.value

  override def onRuntimeAvailable(jeiRuntime: IJeiRuntime): Unit = {
    Option(jeiRuntime)
      .map(JeiRuntime)
      .pipe(r => state.copy(runtime = r))
      .pipe(upsert)
      .tap(_ => refresh(true))
  }

  private def refreshHiddenRecipes(recipes: Seq[Recipe[_]], forced: Boolean, jeiRuntime: JeiRuntime): Unit = {
    val current = state.hiddenRecipes

    val toShow = if (forced) current else recipes.diff(current)
    toShow.groupBy(_.getType.toString).pipe(jeiRuntime.showRecipes)

    val toHide = if (forced) recipes else current.diff(recipes)
    toHide.groupBy(_.getType.toString).pipe(jeiRuntime.hideRecipes)
  }

  private def refresh(forced: Boolean = false): Unit = {
    restrictionRegistry
      .entries
      .filter(_.isType(RestrictionType.Recipe()))
      .asInstanceOf[List[RecipeRestriction]]
      .map(_.target)
      .tap(n => state.execute(refreshHiddenRecipes(n, forced, _)))
      .pipe(n => state.copy(hiddenRecipes = n))
      .pipe(upsert)
  }
}
