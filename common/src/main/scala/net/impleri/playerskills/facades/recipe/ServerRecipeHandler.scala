package net.impleri.playerskills.facades.recipe

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.facades.GuaranteeResponse
import net.impleri.slab.entity.Player
import net.impleri.slab.item.crafting.CraftingContainer
import net.impleri.slab.menu.ContainerMenu
import net.impleri.slab.server.Server
import net.impleri.slab.world.Level

object ServerRecipeHandler extends GuaranteeResponse {
  def handleGetRecipeFor(
    playerV: Player.Vanilla,
    level: Level.Vanilla,
    containerV: CraftingContainer.Vanilla,
    menuV: ContainerMenu.Vanilla,
  ): Boolean =
    guarantee {
      for {
        player <- Player.fromVanilla(playerV)
        server <- Server.fromLevel(level)
        container <- CraftingContainer.fromVanilla(containerV)
        menu <- ContainerMenu.fromVanilla(menuV)
        recipe <- container.getCraftingRecipe(server)
      } yield {
        val canCraft = PlayerSkills.STATE.RECIPE_RESTRICTIONS.isProducible(player, recipe, None)

        if (!canCraft) {
          player.sendEmptyContainerSlot(menu)
        }

        canCraft
      }
    }
}
