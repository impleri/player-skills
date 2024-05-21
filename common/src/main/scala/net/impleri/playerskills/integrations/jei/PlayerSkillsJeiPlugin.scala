package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.IModPlugin
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.client.EventHandler
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.integrations.jei.facades.JeiRuntime
import net.impleri.playerskills.restrictions.recipe.RecipeRestriction
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

@REIPluginCompatIgnore
case class PlayerSkillsJeiPlugin(
  restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  clientEventHandler: EventHandler = PlayerSkillsClient.EVENTS,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends IModPlugin {
  private var current: Seq[Recipe[_]] = Seq.empty

  private var runtime: Option[JeiRuntime] = None

  def registerEvents(): Unit = {
    clientEventHandler.onSkillsUpdate { event => refresh(event.forced) }
  }

  override def getPluginUid: ResourceLocation = new ResourceLocation(PlayerSkills.MOD_ID, "jei_plugin")

  override def onRuntimeAvailable(jeiRuntime: IJeiRuntime): Unit = {
    runtime = Option(JeiRuntime(jeiRuntime))
    refresh(true)
  }

  private def refreshHiddenRecipes(recipes: Seq[Recipe[_]], forced: Boolean, jeiRuntime: JeiRuntime): Unit = {
    val toShow = if (forced) current else recipes.diff(current)
    toShow.groupBy(_.getType.toString).pipe(jeiRuntime.showRecipes)

    val toHide = if (forced) recipes else current.diff(recipes)
    toHide.groupBy(_.getType.toString).pipe(jeiRuntime.hideRecipes)
  }

  private def refresh(forced: Boolean = false): Unit = {
    val next = restrictionRegistry
      .entries
      .filter(_.isType(RestrictionType.Recipe()))
      .asInstanceOf[List[RecipeRestriction]]
      .map(_.target)

    runtime.foreach(refreshHiddenRecipes(next, forced, _))

    current = next
  }
}
