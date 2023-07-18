package net.impleri.playerskills.utils

import net.impleri.playerskills.api.ItemRestrictions
import net.minecraft.world.item.Item

object ListDiff {
  fun contains(a: List<Item>, b: List<Item>): Boolean {
    return getMissing(a, b).isEmpty()
  }

  fun getMissing(a: List<Item>, b: List<Item>): List<Item> {
    if (a.isEmpty() && b.isEmpty()) {
      return ArrayList()
    }

    val bStrings = b.map { ItemRestrictions.getName(it) }

    return a.filter { !bStrings.contains(ItemRestrictions.getName(it)) }
  }
}
