package net.impleri.playerskills.integrations.trinkets

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketComponent
import dev.emi.trinkets.api.TrinketsApi
import net.impleri.playerskills.api.ItemRestrictions
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.function.Consumer

object TrinketsSkills {
  fun onServerTick(server: MinecraftServer) {
    server.playerList.players.forEach(Consumer { onPlayerTick(it) })
  }

  private fun onPlayerTick(player: Player) {
    TrinketsApi.getTrinketComponent(player).ifPresent(handleTrinket(player))
  }

  private fun handleTrinket(player: Player): Consumer<TrinketComponent> {
    return Consumer<TrinketComponent> {
      it.allEquipped.forEach {
        val stack = it.b
        if (!ItemRestrictions.canEquip(player, stack, null)) {
          val slot: SlotReference = it.a
          slot.inventory().setItem(slot.index(), ItemStack.EMPTY)
          ItemRestrictions.moveItemIntoInventory(player, stack)
        }
      }
    }
  }
}
