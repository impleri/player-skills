package net.impleri.playerskills.facades.minecraft.crafting

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionOps
import net.impleri.playerskills.PlayerSkills
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

import scala.util.chaining.scalaUtilChainingOps

case class CraftingMenu(
  private val recipeRestrictions: RecipeRestrictionOps = RecipeRestrictionOps(),
) {
  private def getCraftingRecipe(container: CraftingContainer, server: Server): Option[Recipe[CraftingContainer]] = {
    server.getRecipeManager.getRecipeFor(RecipeType.CRAFTING, container, server)
  }

  private def filterRecipeFor(player: Player[_], container: CraftingContainer, server: Server): Boolean = {
    getCraftingRecipe(container, server)
      .fold(RestrictionsOps.DEFAULT_RESPONSE)(recipeRestrictions.isProducible(player, _, None))
  }
}

object CraftingMenu {
  def handleGetRecipeFor[T <: McPlayer](
    p: T,
    level: Level,
    container: CraftingContainer,
    menu: AbstractContainerMenu,
  ): Boolean = {
    val server = Server(level)
    val player = Player(p)

    CraftingMenu(PlayerSkills.STATE.RECIPE_RESTRICTIONS).filterRecipeFor(player, container, server)
      .tap(v => if (!v) player.sendEmptyContainerSlot(menu))
  }
}
