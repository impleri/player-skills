package net.impleri.slab.entity

import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.entity.{EntityType => McEntityType}

case class EntityType[T <: EntityType.AnyVanilla](override val underlying: T)
    extends ResourceWrapper[T] {
  override val name: Option[ResourceLocation] =
    Registry.Entities.getKey(this.asInstanceOf[EntityType[EntityType.AnyVanilla]])
}

object EntityType {
  type Vanilla[T <: Entity.Vanilla] = McEntityType[T]
  type AnyVanilla = Vanilla[_]
  type Any = EntityType[AnyVanilla]
}
