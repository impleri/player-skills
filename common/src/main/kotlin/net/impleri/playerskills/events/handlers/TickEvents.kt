package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.TickEvent
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.player.Player

class TickEvents {
  fun registerEventHandlers() {
    TickEvent.PLAYER_POST.register(TickEvent.Player { onPlayerTick(it) })
  }

  private fun onPlayerTick(player: Player) {
    if (player.getLevel().isClientSide) {
      return
    }
    val inventory = player.inventory

    // Move unwearable items from armor and offhand into normal inventory
    ItemRestrictions.filterFromList(player, inventory.armor)
    ItemRestrictions.filterFromList(player, inventory.offhand)

    // Get unholdable items from inventory
    val itemsToRemove = ItemRestrictions.getItemsToRemove(player, inventory.items)

    // Drop the unholdable items from the normal inventory
    if (itemsToRemove.isNotEmpty()) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} is holding ${itemsToRemove.size} item(s) that should be dropped")
      itemsToRemove.forEach(ItemRestrictions.dropFromInventory(player))
    }
  }
}
