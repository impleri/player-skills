package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.IModPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.recipe.IFocus
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.client.api.ItemRestrictionClient
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.items.ItemSkills
import net.impleri.playerskills.utils.ListDiff
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@REIPluginCompatIgnore
open class PlayerSkillsJeiPlugin : IModPlugin {
  private var runtime: IJeiRuntime? = null
  private val unconsumables: MutableList<Item> = ArrayList()
  private val unproducibles: MutableList<Item> = ArrayList()

  init {
    ClientSkillsUpdatedEvent.EVENT.register { event: ClientSkillsUpdatedEvent -> updateHidden(event) }
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
      ItemSkills.LOGGER.warn("JEI Runtime not yet available to update")
      return
    }

    refresh(event.forced)
  }

  private fun refresh(force: Boolean) {
    if (force) {
      unconsumables.clear()
      unproducibles.clear()
    }

    ItemSkills.LOGGER.debug("Updating JEI item restrictions (forced? $force)")

    processUnconsumables()
    processUnproducibles()
  }

  private fun processUnconsumables() {
    val manager = runtime?.ingredientManager ?: return
    val next = ItemRestrictionClient.unconsumable

    // Nothing on either list, so don't bother
    if (unconsumables.isEmpty() && next.isEmpty()) {
      ItemSkills.LOGGER.debug("No changes in restrictions")
      return
    }

    ItemSkills.LOGGER.debug("Found ${next.size} unconsumable item(s)")

    val toShow = ListDiff.getMissing(unconsumables, next)
    if (toShow.isNotEmpty()) {
      ItemSkills.LOGGER.debug(
        "Showing ${toShow.size} item(s) based on consumables: ${toShow.map { ItemRestrictions.getName(it) }}",
      )
      manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, getItemStack(toShow))
    }

    val toHide = ListDiff.getMissing(next, unconsumables)
    if (toHide.isNotEmpty()) {
      ItemSkills.LOGGER.debug(
        "Hiding ${toHide.size} item(s) based on consumables: ${toHide.map { ItemRestrictions.getName(it) }}",
      )
      manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, getItemStack(toHide))
    }

    unconsumables.clear()
    unconsumables.addAll(next)
  }

  private fun processUnproducibles() {
    val next = ItemRestrictionClient.unproducible

    // Nothing on either list, so don't bother
    if (unproducibles.isEmpty() && next.isEmpty()) {
      ItemSkills.LOGGER.debug("No changes in restrictions")
      return
    }

    ItemSkills.LOGGER.debug("Found ${next.size} unproducible item(s)")

    val toShow = ListDiff.getMissing(unproducibles, next)
    if (toShow.isNotEmpty()) {
      val foci = getFociFor(toShow)
      val types = getTypesFor(foci, true)
      ItemSkills.LOGGER.debug(
        "Showing ${toShow.size} item(s) based on producibles: ${toShow.map { ItemRestrictions.getName(it) }}",
      )
      types.forEach { showRecipesForType(it, foci) }
    }

    val toHide = ListDiff.getMissing(next, unproducibles)
    if (toHide.isNotEmpty()) {
      val foci = getFociFor(toHide)
      val types = getTypesFor(foci, false)
      ItemSkills.LOGGER.debug(
        "Hiding ${toHide.size} item(s) based on producibles: ${toHide.map { ItemRestrictions.getName(it) }}",
      )
      types.forEach { hideRecipesForType(it, foci) }
    }

    unproducibles.clear()
    unproducibles.addAll(next)
  }

  private fun getFociFor(items: List<Item>): List<IFocus<ItemStack>> {
    val factory = runtime?.jeiHelpers?.focusFactory ?: return ArrayList()

    return getItemStack(items)
      .map { factory.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, it) }
  }

  private fun getTypesFor(foci: List<IFocus<ItemStack>>, includeHidden: Boolean): Collection<RecipeType<*>> {
    val lookup = runtime?.recipeManager?.createRecipeCategoryLookup()?.limitFocus(foci) ?: return ArrayList()

    if (includeHidden) {
      lookup.includeHidden()
    }

    return lookup.get()
      .map { it.recipeType }
      .toList()
  }

  private fun <T> hideRecipesForType(type: RecipeType<T>, foci: List<IFocus<ItemStack>>) {
    runtime?.recipeManager?.hideRecipes(type, getRecipesFor(type, foci, false))
  }

  private fun <T> showRecipesForType(type: RecipeType<T>, foci: List<IFocus<ItemStack>>) {
    runtime?.recipeManager?.unhideRecipes(type, getRecipesFor(type, foci, true))
  }

  private fun <T> getRecipesFor(
    type: RecipeType<T>,
    foci: List<IFocus<ItemStack>>,
    includeHidden: Boolean,
  ): Collection<T> {
    val lookup = runtime?.recipeManager
      ?.createRecipeLookup(type)
      ?.limitFocus(foci) ?: return ArrayList()

    if (includeHidden) {
      lookup.includeHidden()
    }

    return lookup.get().toList()
  }

  private fun getItemStack(items: List<Item>): Collection<ItemStack> {
    return items.map { ItemStack(it) }
  }
}
