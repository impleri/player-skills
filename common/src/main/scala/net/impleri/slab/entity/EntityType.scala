package net.impleri.slab.entity

import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.entity.{EntityType => McEntityType}

case class EntityType[T <: McEntityType[_]](override val underlying: T)
    extends ResourceWrapper[T] {
  override val name: Option[ResourceLocation] =
    Registry.Entities.getKey(underlying.asInstanceOf)
}

object EntityType {
  type Any = EntityType[_]
  type Vanilla[T <: Entity.Vanilla] = McEntityType[T]
  type AnyVanilla = Vanilla[_]
}
