package net.impleri.playerskills.integrations.curios

import net.impleri.playerskills.api.ItemRestrictions
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import top.theillusivec4.curios.api.CuriosApi

object CuriosSkills {
  fun onPlayerTick(player: Player) {
    CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handleCurios(player))
  }

  private fun handleCurios(player: Player): (IItemHandlerModifiable) -> Unit {
    return {
      val slotCount = it.slots
      if (slotCount > 0) {
        for (idx in 0..slotCount) {
          val slot: ItemStack = it.getStackInSlot(idx)
          if (!ItemRestrictions.canEquip(player, slot, null)) {
            val badItem = it.extractItem(idx, slot.count, false)
            ItemRestrictions.moveItemIntoInventory(player, badItem)
          }
        }
      }
    }
  }
}
