package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.jei.facades.JeiRuntime
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

@REIPluginCompatIgnore
@JeiPlugin
class PlayerSkillsJeiPlugin extends IModPlugin {
  lazy val helper: JeiHelper = JeiHelper()

  override def getPluginUid: ResourceLocation.Vanilla = ResourceLocation(PlayerSkills.MOD_ID, "jei_plugin").get.value

  override def onRuntimeAvailable(jeiRuntime: IJeiRuntime): Unit = {
    Option(jeiRuntime)
      .map(JeiRuntime)
      .tap(helper.updateRuntime)
  }
}
