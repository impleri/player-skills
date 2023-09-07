package net.impleri.playerskills.utils

import net.minecraft.resources.ResourceLocation

object ListDiff {
  fun <T> contains(a: List<T>, b: List<T>, getName: (T) -> ResourceLocation): Boolean {
    return getMissing(a, b, getName).isEmpty()
  }

  fun <T> getMissing(a: List<T>, b: List<T>, getName: (T) -> ResourceLocation): List<T> {
    if (a.isEmpty() && b.isEmpty()) {
      return ArrayList()
    }

    val bStrings = b.map { getName(it) }

    return a.filter { !bStrings.contains(getName(it)) }
  }
}
