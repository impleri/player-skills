package net.impleri.playerskills.restrictions.conditions

import net.minecraft.resources.ResourceLocation

trait ReplacementConditions[T] {
  var replacement: Option[T] = None

  def replaceWith(replacement: ResourceLocation): Unit = ???
}
