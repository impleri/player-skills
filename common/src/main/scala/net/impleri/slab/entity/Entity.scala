package net.impleri.slab.entity

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.world.Biome
import net.impleri.slab.world.Position
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

import scala.util.Try

class Entity[T <: McEntity](private val underlying: T) {
  lazy val name: String = underlying.getName.getString

  lazy val level: Level = underlying.getLevel

  lazy val mobType: EntityType[_] = underlying.getType

  lazy val mobTypeName: String = mobType.toString

  lazy val dimension: Option[ResourceLocation] = Try(level.dimension().location()).toOption.flatMap(ResourceLocation(_))

  lazy val biome: Option[Biome] = biomeAt()

  lazy val position: Option[Position] = Try(underlying.getOnPos).toOption.map(Position(_))

  def biomeAt(pos: Option[Position] = None): Option[Biome] = {
    pos
      .orElse(position)
      .map(p => level.getBiome(p.raw))
      .map(Biome.apply)
  }

  def biomeNameAt(pos: Option[Position] = None): Option[ResourceLocation] = {
    biomeAt(pos).flatMap(_.name)
  }

  def isEmpty: Boolean = Option(underlying).isEmpty

  def asOption: Option[Entity[T]] = if (isEmpty) None else Option(this)

  def isPlayer: Boolean = underlying.isInstanceOf[McPlayer]

  def asPlayer[P <: McPlayer]: Player[P] = Player(underlying.asInstanceOf[P])
}

object Entity {
  type Any = Entity[_]

  def apply[T <: McEntity](entity: T): Entity[T] = new Entity(entity)
}
