package net.impleri.slab.world

import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.chunk.{ChunkAccess, LevelChunk}

case class Chunk[T <: Chunk.BaseVanilla](underlying: T) {
  private[slab] def pos = underlying.getPos

  private def handleUpdate(f: => Unit): Chunk[T] = {
    f
    underlying.setUnsaved(true)

    copy(underlying = underlying)
  }

  def setBiome(biome: Biome, climateSampler: Climate.Sampler): Chunk[T] =
    handleUpdate {
      underlying.fillBiomesFromNoise(
        biome.asSource,
        climateSampler,
      )
    }

  override def equals(obj: Any): Boolean = obj match {
    case c: Chunk[_] => this.pos.equals(c.pos)
    case _ => false
  }
}

object Chunk {
  type BaseVanilla = ChunkAccess
  type Vanilla = LevelChunk

  type Any = Chunk[_]
}
