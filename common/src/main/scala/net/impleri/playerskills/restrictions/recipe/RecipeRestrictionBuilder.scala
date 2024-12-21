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

import scala.util.chaining.scalaUtilChainingOps

case class RecipeRestrictionBuilder(
  protected val serverState: ServerStateContainer = ServerStateContainer(),
  protected val restrictionRegistry: RestrictionRegistry =
    RestrictionRegistry(),
  protected val recipeTypeRegistry: Registry.RECIPE_TYPE = Registry.RecipeTypes,
  override val logger: Logger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[Recipe.Any, Recipe.AnyVanilla, RecipeConditions] {
  override val singleAsString = true

  private def restrictRecipe(
    recipe: Recipe.Any,
    builder: RecipeConditions,
  ): Unit =
    RecipeRestriction(recipe, builder)
      .tap(restrictionRegistry.add)
      .tap(logRestriction(
      recipe.name.fold(s"${recipe.getResultItem.name}")(_.asString),
        _,
    ))

  private def restrictRecipes[R <: Recipe.BaseVanilla](
    recipeType: RecipeType.Any,
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit =
    for {
      manager <- serverState.SERVER.map(_.getRecipeManager).toList
      recipe <- manager.getAllFor(recipeType)
      if target.matches(recipe)
    } yield restrictRecipe(recipe, builder)

  private def restrictTarget(
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit =
    for {
      recipeType <- recipeTypeRegistry.get(target.recipeType)
    } yield restrictRecipes(recipeType, target, builder)

  override protected def restrictString(
    targetName: String,
    builder: RecipeConditions,
  ): Unit =
    logger.error(s"Unused path")

  override protected def restrictOne(
    targetName: ResourceLocation,
    builder: RecipeConditions,
  ): Unit =
    logger.error(s"Unused path")

  override def restrict(data: (String, RecipeConditions)): Unit =
    for {
      target <- data._2.targets
    } yield restrictTarget(target, data._2)

  def add(builder: RecipeConditions): Unit =
    restrictions += s"recipe-${restrictions.size}" -> builder
}
