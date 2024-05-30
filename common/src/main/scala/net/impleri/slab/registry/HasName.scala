package net.impleri.slab.registry

import net.impleri.slab.resources.ResourceLocation

trait HasName {
  def getName: Option[ResourceLocation]
}
