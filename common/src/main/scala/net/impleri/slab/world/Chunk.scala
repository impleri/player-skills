package net.impleri.slab.world

import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.chunk.{ChunkAccess, LevelChunk}

case class Chunk[T <: Chunk.BaseVanilla](underlying: T) {
  def setBiome(biome: Biome, climateSampler: Climate.Sampler): Unit =
    underlying.fillBiomesFromNoise(
      biome.asSource,
      climateSampler,
    )
}

object Chunk {
  type BaseVanilla = ChunkAccess
  type Vanilla = LevelChunk

  type Any = Chunk[_]
}
