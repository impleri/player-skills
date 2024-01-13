package net.impleri.playerskills.facades.minecraft

import net.minecraft.resources.ResourceLocation

trait HasName {
  def getName: Option[ResourceLocation]
}
