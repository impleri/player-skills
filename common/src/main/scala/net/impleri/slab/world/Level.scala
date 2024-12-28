package net.impleri.slab.world

import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.level.LevelAccessor

import scala.util.Try

case class Level[T <: Level.BasVanilla](private val underlying: T) {
  private def asFull = Option(underlying)
    .filter(_.isInstanceOf[Level.Vanilla])
    .map(_.asInstanceOf[Level.Vanilla])

  def getDimension: Option[ResourceKey[Level.Vanilla]] = asFull
    .map(_.dimension())
    .map(ResourceKey(_))

  def getDimensionName: Option[ResourceLocation] = getDimension.flatMap(_.name)

  def getBiome(pos: Position): Option[Biome] =
    Try(underlying.getBiome(pos.value))
      .toOption
      .map(Biome(_))
}

object Level {
  type Any = Level[_]

  type BasVanilla = LevelAccessor

  type Vanilla = McLevel
}
