package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.IModPlugin
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.Fluid

@REIPluginCompatIgnore
open class PlayerSkillsJeiPlugin<T> : IModPlugin, ItemsHandler, FluidsHandler<T> {
  override var runtime: IJeiRuntime? = null
  override val unconsumableItems: MutableList<Item> = ArrayList()
  override val unconsumableFluids: MutableList<Fluid> = ArrayList()
  override val unproducibleItems: MutableList<Item> = ArrayList()
  override val unproducibleFluids: MutableList<Fluid> = ArrayList()

  init {
    ClientSkillsUpdatedEvent.EVENT.register { updateHidden(it) }
  }

  override fun getPluginUid(): ResourceLocation {
    return ResourceLocation(PlayerSkills.MOD_ID, "jei_plugin")
  }

  override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
    runtime = jeiRuntime
    refresh(true)
  }

  private fun updateHidden(event: ClientSkillsUpdatedEvent) {
    if (runtime == null) {
      PlayerSkillsLogger.SKILLS.warn("JEI Runtime not yet available to update")
      return
    }

    refresh(event.forced)
  }

  private fun refresh(force: Boolean) {
    if (force) {
      unconsumableItems.clear()
      unconsumableFluids.clear()
      unproducibleItems.clear()
      unproducibleFluids.clear()
    }

    PlayerSkillsLogger.SKILLS.debug("Updating JEI restrictions (forced? $force)")

    processUnconsumableItems()
    processUnconsumableFluids()
    processUnproducibleItems()
    processUnproducibleFluids()
  }
}
