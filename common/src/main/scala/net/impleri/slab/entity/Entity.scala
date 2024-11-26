package net.impleri.slab.entity

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.world.Biome
import net.impleri.slab.world.Level
import net.impleri.slab.world.Position
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.LivingEntity

import scala.util.Try

class Entity[T <: Entity.Vanilla](override val underlying: T)
    extends ResourceWrapper[T] {
  lazy val name: Option[ResourceLocation] = getType.flatMap(_.name)

  lazy val position: Option[Position] =
    Try(underlying.getOnPos).toOption.map(Position(_))

  lazy val getType: Option[EntityType[_]] =
    Option(underlying.getType).map(EntityType(_))

  lazy val level: Option[Level.Any] = Option(underlying.getLevel).map(Level(_))

  lazy val mobTypeName: String = getType.toString

  lazy val dimension: Option[ResourceLocation] =
    level.flatMap(_.getDimensionName)

  lazy val biome: Option[Biome] = biomeAt()

  def biomeAt(pos: Option[Position] = None): Option[Biome] =
    for {
      at <- pos.orElse(position)
      lvl <- level
      biome <- lvl.getBiome(at)
    } yield biome

  def isEmpty: Boolean = Option(underlying).isEmpty

  def asOption: Option[Entity[T]] = if (isEmpty) None else Option(this)

  def isPlayer: Boolean = underlying.isInstanceOf[Player.Vanilla]

  def asPlayer[P <: Player.Vanilla]: Player = Player(
    underlying.asInstanceOf[P],
  )
}

object Entity {
  type Any = Entity[_]

  type Vanilla = McEntity

  type Living = LivingEntity

  def apply[T <: Vanilla](entity: T): Entity[T] = new Entity(entity)
}
