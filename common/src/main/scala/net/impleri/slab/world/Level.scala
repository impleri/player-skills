package net.impleri.slab.world

import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.server.level.{ServerChunkCache, ServerLevel}
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.biome.Climate

import scala.util.Try

case class Level[T <: Level.BaseVanilla](private val underlying: T) {
  private def asFull = Option(underlying)
    .filter(_.isInstanceOf[Level.Vanilla])
    .map(_.asInstanceOf[Level.Vanilla])

  def getDimension: Option[ResourceKey[Level.Vanilla]] = asFull
    .map(_.dimension())
    .map(ResourceKey(_))

  def getDimensionName: Option[ResourceLocation] = getDimension.flatMap(_.name)

  private[slab] def getClimateSampler: Option[Climate.Sampler] =
    underlying match {
      case sl: ServerLevel => Option(sl.getChunkSource.asInstanceOf[ServerChunkCache].randomState().sampler())
      case _ => None
    }

  def getChunksBetween(from: Coordinates, to: Coordinates): Seq[Chunk.Any] = {
    val chunks = for {
      x <- from.x.toLong to to.x.toLong
      if x % 16 == 0
      z <- from.z.toLong to to.z.toLong
      if z % 16 == 0
    } yield {
      Coordinates(x.toDouble, from.y, z.toDouble)
        .map(_.toPosition)
        .flatMap(getChunk)
    }

    (Seq(getChunk(from.toPosition)) ++ chunks).flatten
  }

  def getChunk(pos: Position): Option[Chunk.Any] = {
    Try(underlying.getChunk(pos.value)).toOption.map(Chunk(_))
  }

  def getBiome(pos: Position): Option[Biome] =
    Try(underlying.getBiome(pos.value))
      .toOption
      .map(Biome(_))
}

object Level {
  type Any = Level[_]

  type BaseVanilla = LevelAccessor

  type Vanilla = McLevel
}
