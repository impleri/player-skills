package net.impleri.slab.advancements

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.advancements.Advancement

case class Award(protected val underlying: Award.Vanilla) extends ResourceWrapper[Award.Vanilla] {
  override val name: Option[ResourceLocation] = Option(underlying.getId).flatMap(ResourceLocation(_))

  override val value: Award.Vanilla = underlying
}

object Award {
  type Vanilla = Advancement
}
