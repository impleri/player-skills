package net.impleri.playerskills.facades.item

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.slab.entity.Player
import net.impleri.slab.item.crafting.CraftingContainer
import net.impleri.slab.menu.ContainerMenu
import net.impleri.slab.server.Server

import scala.util.chaining.scalaUtilChainingOps

object CraftingMenuHandler {
  private def getRecipeFor(
    player: Player.Any,
    server: Server,
    container: CraftingContainer,
    menu: ContainerMenu.Any,
  ): Option[Boolean] = {
    container
      .getCraftingRecipe(server)
      .map(PlayerSkills.STATE.RECIPE_RESTRICTIONS.isProducible(player, _, None))
      .tap(v => if (v.contains(false)) player.sendEmptyContainerSlot(menu))
  }

  def handleGetRecipeFor(
    player: Option[Player.Any],
    server: Option[Server],
    container: Option[CraftingContainer],
    menu: Option[ContainerMenu.Any],
  ): Boolean = {
    player
      .flatMap(p =>
        server.flatMap(s =>
          container.flatMap(c => menu.flatMap(m => getRecipeFor(p, s, c, m))),
        ),
      )
      .fold(RestrictionsOps.DEFAULT_RESPONSE)(identity)
  }
}
