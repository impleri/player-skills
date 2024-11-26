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
    player: Player,
    server: Server,
    container: CraftingContainer,
    menu: ContainerMenu.Any,
  ): Option[Boolean] =
    container
      .getCraftingRecipe(server)
      .map(PlayerSkills.STATE.RECIPE_RESTRICTIONS.isProducible(player, _, None))
      .tap(v => if (v.contains(false)) player.sendEmptyContainerSlot(menu))

  private def guarantee(response: Option[Boolean]) = response.getOrElse(RestrictionsOps.DEFAULT_RESPONSE)

  def handleGetRecipeFor(
    playerOpt: Option[Player],
    serverOpt: Option[Server],
    containerOpt: Option[CraftingContainer],
    menuOpt: Option[ContainerMenu.Any],
  ): Boolean =
    guarantee {
      for {
        player <- playerOpt
        server <- serverOpt
        container <- containerOpt
        menu <- menuOpt
        response <- getRecipeFor(player, server, container, menu)
      } yield response
    }
}
