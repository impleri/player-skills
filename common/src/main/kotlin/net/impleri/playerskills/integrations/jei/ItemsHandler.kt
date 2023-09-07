package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.recipe.IFocus
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.client.api.ItemRestrictionClient
import net.impleri.playerskills.utils.ListDiff
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@REIPluginCompatIgnore
internal interface ItemsHandler {
  var runtime: IJeiRuntime?
  val unconsumableItems: MutableList<Item>
  val unproducibleItems: MutableList<Item>

  fun processUnconsumableItems() {
    val manager = runtime?.ingredientManager ?: return
    val next = ItemRestrictionClient.unconsumable

    // Nothing on either list, so don't bother
    if (unconsumableItems.isEmpty() && next.isEmpty()) {
      PlayerSkillsLogger.ITEMS.debug("No changes in restrictions")
      return
    }

    PlayerSkillsLogger.ITEMS.debug("Found ${next.size} unconsumable item(s)")

    val toShow = ListDiff.getMissing(unconsumableItems, next) { ItemRestrictions.getName(it) }
    if (toShow.isNotEmpty()) {
      PlayerSkillsLogger.ITEMS.debug(
        "Showing ${toShow.size} item(s) based on consumables: ${toShow.map { ItemRestrictions.getName(it) }}",
      )
      manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, getItemStack(toShow))
    }

    val toHide = ListDiff.getMissing(next, unconsumableItems) { ItemRestrictions.getName(it) }
    if (toHide.isNotEmpty()) {
      PlayerSkillsLogger.ITEMS.debug(
        "Hiding ${toHide.size} item(s) based on consumables: ${toHide.map { ItemRestrictions.getName(it) }}",
      )
      manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, getItemStack(toHide))
    }

    unconsumableItems.clear()
    unconsumableItems.addAll(next)
  }

  fun processUnproducibleItems() {
    val next = ItemRestrictionClient.unproducible

    // Nothing on either list, so don't bother
    if (unproducibleItems.isEmpty() && next.isEmpty()) {
      PlayerSkillsLogger.ITEMS.debug("No changes in restrictions")
      return
    }

    PlayerSkillsLogger.ITEMS.debug("Found ${next.size} unproducible item(s)")

    val toShow = ListDiff.getMissing(unproducibleItems, next) { ItemRestrictions.getName(it) }
    if (toShow.isNotEmpty()) {
      val foci = getFociFor(toShow)
      val types = getTypesFor(foci, true)
      PlayerSkillsLogger.ITEMS.debug(
        "Showing ${toShow.size} item(s) based on producibles: ${toShow.map { ItemRestrictions.getName(it) }}",
      )
      types.forEach { showRecipesForType(it, foci) }
    }

    val toHide = ListDiff.getMissing(next, unproducibleItems) { ItemRestrictions.getName(it) }
    if (toHide.isNotEmpty()) {
      val foci = getFociFor(toHide)
      val types = getTypesFor(foci, false)
      PlayerSkillsLogger.ITEMS.debug(
        "Hiding ${toHide.size} item(s) based on producibles: ${toHide.map { ItemRestrictions.getName(it) }}",
      )
      types.forEach { hideRecipesForType(it, foci) }
    }

    unproducibleItems.clear()
    unproducibleItems.addAll(next)
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
