package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

trait HasName {
  def getName: Option[ResourceLocation]
}
