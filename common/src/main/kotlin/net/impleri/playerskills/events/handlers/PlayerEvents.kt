package net.impleri.playerskills.events.handlers

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.PlayerEvent
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

internal class PlayerEvents {
  fun registerEvents() {
    PlayerEvent.PLAYER_JOIN.register(PlayerEvent.PlayerJoin { onPlayerJoin(it) })
    PlayerEvent.PLAYER_QUIT.register(PlayerEvent.PlayerQuit { onPlayerQuit(it) })

    PlayerEvent.PICKUP_ITEM_PRE.register(
      PlayerEvent.PickupItemPredicate { player: Player, _: Entity, stack: ItemStack ->
        beforePlayerPickup(
          player,
          stack,
        )
      },
    )
  }

  private fun onPlayerJoin(player: ServerPlayer) {
    Players.open(player.uuid)
    NetHandler.syncPlayer(player, true)
  }

  private fun beforePlayerPickup(player: Player, stack: ItemStack): EventResult {
    val item = ItemRestrictions.getValue(stack)
    val itemName = ItemRestrictions.getName(item)

    if (!ItemRestrictions.canHold(player, item, null)) {
      PlayerSkillsLogger.ITEMS.debug("${player.name.string} cannot pickup $itemName")
      return EventResult.interruptFalse()
    }

    PlayerSkillsLogger.SKIPS.debug("${player.name.string} is going to pickup $itemName")
    return EventResult.pass()
  }

  private fun onPlayerQuit(player: ServerPlayer) {
    Players.close(player.uuid)
  }
}
