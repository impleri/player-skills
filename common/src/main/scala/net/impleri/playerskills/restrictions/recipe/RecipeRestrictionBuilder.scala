package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.restrictions.RestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.item.crafting.Recipe
import net.impleri.slab.item.crafting.RecipeType
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation

case class RecipeRestrictionBuilder(
  protected val serverState: ServerStateContainer = ServerStateContainer(),
  protected val restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  protected val recipeTypeRegistry: Registry.RECIPE_TYPE = Registry.RecipeTypes,
  override val logger: Logger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[Recipe.Any, Recipe.AnyVanilla, RecipeConditions] {
  override val singleAsString = true

  private def restrictRecipe(recipe: Recipe.Any, builder: RecipeConditions): Unit = {
    val restriction = RecipeRestriction(recipe, builder)

    restrictionRegistry.add(restriction)
    logRestriction(recipe.name.fold(s"${recipe.getResultItem.name}")(_.asString), restriction)
  }

  def add(builder: RecipeConditions): Unit = {
    restrictions += s"recipe-${restrictions.size}" -> builder
  }

  private def restrictRecipes[R <: Recipe.BaseVanilla](
    recipeType: RecipeType.Any,
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit = {
    serverState.SERVER
      .map(_.getRecipeManager)
      .toList
      .flatMap(_.getAllFor[R](recipeType))
      .filter(target.matches)
      .foreach(restrictRecipe(_, builder))
  }

  private def restrictTarget(
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit = {
    recipeTypeRegistry.get(target.recipeType)
      .foreach(t => restrictRecipes(t, target, builder))
  }

  override def restrict(data: (String, RecipeConditions)): Unit = {
    data._2.targets.foreach(restrictTarget(_, data._2))
  }

  override protected def restrictString(
    targetName: String,
    builder: RecipeConditions,
  ): Unit = {
    logger.error(s"Unused path")
  }

  override protected def restrictOne(
    targetName: ResourceLocation,
    builder: RecipeConditions,
  ): Unit = {
    logger.error(s"Unused path")
  }
}
