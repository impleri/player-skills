package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Biome
import net.minecraft.world.entity.{Entity => MinecraftEntity}
import net.minecraft.world.entity.player.{Player => MinecraftPlayer}
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

import scala.util.Try

class Entity[T <: MinecraftEntity](private val entity: T) {
  lazy val name: String = entity.getName.getString

  lazy val level: Level = entity.getLevel

  lazy val mobType: EntityType[_] = entity.getType

  lazy val mobTypeName: String = mobType.toString

  lazy val dimension: Option[ResourceLocation] = Try(level.dimension().location()).toOption.map(ResourceLocation(_))

  lazy val biome: Option[Biome] = biomeAt()

  lazy val position: Option[Position] = Try(entity.getOnPos).toOption.map(Position(_))

  def biomeAt(pos: Option[Position] = None): Option[Biome] = {
    pos
      .orElse(position)
      .map(p => level.getBiome(p.raw))
      .map(Biome.apply)
  }

  def biomeNameAt(pos: Option[Position] = None): Option[ResourceLocation] = {
    biomeAt(pos).flatMap(_.name)
  }

  def isEmpty: Boolean = Option(entity).isEmpty

  def asOption: Option[Entity[T]] = if (isEmpty) None else Option(this)

  def isPlayer: Boolean = entity.isInstanceOf[MinecraftPlayer]

  def asPlayer[P <: MinecraftPlayer]: Player[P] = Player(entity.asInstanceOf[P])
}

object Entity {
  def apply[T <: MinecraftEntity](entity: T): Entity[T] = new Entity(entity)
}
