package net.impleri.playerskills.integrations.jei

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes
import mezz.jei.api.recipe.IFocus
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.runtime.IJeiRuntime
import net.impleri.playerskills.api.FluidRestrictions
import net.impleri.playerskills.client.api.FluidRestrictionClient
import net.impleri.playerskills.utils.ListDiff
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.level.material.Fluid

@REIPluginCompatIgnore
internal interface FluidsHandler<T> {
  var runtime: IJeiRuntime?
  val unconsumableFluids: MutableList<Fluid>
  val unproducibleFluids: MutableList<Fluid>

  private fun getFluidType(): IIngredientTypeWithSubtypes<Fluid, T> {
    return runtime!!.jeiHelpers.platformFluidHelper.fluidIngredientType as IIngredientTypeWithSubtypes<Fluid, T>
  }

  fun processUnconsumableFluids() {
    val manager = runtime!!.ingredientManager
    val fluidType = getFluidType()
    val next: List<Fluid> = FluidRestrictionClient.INSTANCE.unconsumable

    // Nothing on either list, so don't bother
    if (unconsumableFluids.size == 0 && next.isEmpty()) {
      PlayerSkillsLogger.FLUIDS.debug("No changes in restrictions")
      return
    }

    PlayerSkillsLogger.FLUIDS.debug("Found ${next.size} unconsumable fluid(s)")
    val toShow = ListDiff.getMissing(unconsumableFluids, next) { FluidRestrictions.getFluidName(it) }
    if (toShow.isNotEmpty()) {
      val fluidsToShow = toShow
        .map { FluidRestrictions.getFluidName(it) }
        .toList()
        .toString()
      PlayerSkillsLogger.FLUIDS.debug("Showing ${toShow.size} fluid(s) based on consumables: $fluidsToShow")
      manager.addIngredientsAtRuntime(fluidType, getFluidStacks(toShow))
    }

    val toHide = ListDiff.getMissing(next, unconsumableFluids) { FluidRestrictions.getFluidName(it) }
    if (toHide.isNotEmpty()) {
      val fluidsToHide = toHide
        .map { FluidRestrictions.getFluidName(it) }
        .toList()
        .toString()
      PlayerSkillsLogger.FLUIDS.debug("Hiding ${toHide.size} fluid(s) based on consumables: $fluidsToHide")
      manager.removeIngredientsAtRuntime(fluidType, getFluidStacks(toHide))
    }

    unconsumableFluids.clear()
    unconsumableFluids.addAll(next)
  }

  private fun getFluidStacks(fluids: List<Fluid>): List<T> {
    return fluids
      .map { getFluidStacksFor(it) }
      .toList()
  }

  private fun getFluidStacksFor(fluid: Fluid): T {
    val helper = runtime!!.jeiHelpers.platformFluidHelper
    val bucket = helper.bucketVolume()
    return helper.create(fluid, bucket) as T
  }

  fun processUnproducibleFluids() {
    val next: List<Fluid> = FluidRestrictionClient.INSTANCE.unproducible

    // Nothing on either list, so don't bother
    if (unproducibleFluids.size == 0 && next.isEmpty()) {
      PlayerSkillsLogger.FLUIDS.debug("No changes in restrictions")
      return
    }
    PlayerSkillsLogger.FLUIDS.debug("Found ${next.size} unproducible fluid(s)")

    val toShow = ListDiff.getMissing(unproducibleFluids, next) { FluidRestrictions.getFluidName(it) }
    if (toShow.isNotEmpty()) {
      val foci = getFociFor(getFluidStacks(toShow))
      val types = getTypesFor(foci, true)
      val fluidsToShow = toShow
        .map { FluidRestrictions.getFluidName(it) }
        .toList()
        .toString()
      PlayerSkillsLogger.FLUIDS.debug("Showing ${toShow.size} fluid(s) based on producibles: $fluidsToShow")
      types.forEach { showRecipesForType(it, foci) }
    }

    val toHide = ListDiff.getMissing(next, unproducibleFluids) { FluidRestrictions.getFluidName(it) }
    if (toHide.isNotEmpty()) {
      val foci = getFociFor(getFluidStacks(toHide))
      val types = getTypesFor(foci, false)
      val fluidsToHide = toHide
        .map { FluidRestrictions.getFluidName(it) }
        .toList()
        .toString()
      PlayerSkillsLogger.FLUIDS.debug("Hiding ${toHide.size} fluid(s) based on producibles: $fluidsToHide")
      types.forEach { hideRecipesForType(it, foci) }
    }

    unproducibleFluids.clear()
    unproducibleFluids.addAll(next)
  }

  private fun getFociFor(fluids: List<T>): List<IFocus<T>> {
    val factory = runtime!!.jeiHelpers.focusFactory
    val fluidType = getFluidType()
    return fluids.stream()
      .map { fluid: T -> factory.createFocus(RecipeIngredientRole.OUTPUT, fluidType, fluid) }
      .toList()
  }

  private fun getTypesFor(foci: List<IFocus<T>>, includeHidden: Boolean): Collection<RecipeType<T>> {
    val lookup = runtime!!.recipeManager.createRecipeCategoryLookup()
      .limitFocus(foci)
    if (includeHidden) {
      lookup.includeHidden()
    }
    return lookup.get()
      .map { it.recipeType }
      .map { it as RecipeType<T> }
      .toList()
  }

  private fun hideRecipesForType(type: RecipeType<T>, foci: List<IFocus<T>>) {
    runtime!!.recipeManager.hideRecipes(type, getRecipesFor(type, foci, false))
  }

  private fun showRecipesForType(type: RecipeType<T>, foci: List<IFocus<T>>) {
    runtime!!.recipeManager.unhideRecipes(type, getRecipesFor(type, foci, true))
  }

  private fun getRecipesFor(type: RecipeType<T>, foci: List<IFocus<T>>, includeHidden: Boolean): Collection<T> {
    val lookup = runtime!!.recipeManager
      .createRecipeLookup(type)
      .limitFocus(foci)
    if (includeHidden) {
      lookup.includeHidden()
    }
    return lookup.get().toList()
  }
}
