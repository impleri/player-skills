package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.restrictions.RestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.Container

case class RecipeRestrictionBuilder(
  protected val serverState: ServerStateContainer = ServerStateContainer(),
  protected val restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  protected val recipeTypeRegistry: Registry[RecipeType[_]] = Registry.RecipeTypes,
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[McRecipe[_], RecipeConditions] {
  override val singleAsString = true

  private def restrictRecipe(recipe: Recipe[_], builder: RecipeConditions): Unit = {
    val restriction = RecipeRestriction(recipe, builder)

    restrictionRegistry.add(restriction)
    logRestriction(recipe.getName.fold(s"${recipe.getResultItem.name}")(_.toString), restriction)
  }

  def add(builder: RecipeConditions): Unit = {
    restrictions += s"recipe-${restrictions.size}" -> builder
  }

  private def restrictRecipes[C <: Container, T <: McRecipe[C]](
    recipeType: RecipeType[T],
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit = {
    serverState.SERVER
      .map(_.getRecipeManager)
      .toList
      .flatMap(_.getAllFor[C, T](recipeType))
      .filter(target.matches)
      .foreach(restrictRecipe(_, builder))
  }

  private def restrictTarget[C <: Container, T <: McRecipe[C]](
    target: RecipeTarget,
    builder: RecipeConditions,
  ): Unit = {
    recipeTypeRegistry.get(target.recipeType)
      .asInstanceOf[Option[RecipeType[T]]]
      .foreach(restrictRecipes[C, T](_, target, builder))
  }

  override protected[recipe] def restrictOne(
    targetName: ResourceLocation,
    builder: RecipeConditions,
  ): Unit = {
    builder.targets.foreach(restrictTarget(_, builder))
  }

  override protected[recipe] def restrictString(
    targetName: String,
    builder: RecipeConditions,
  ): Unit = {
    builder.targets.foreach(restrictTarget(_, builder))
  }
}
