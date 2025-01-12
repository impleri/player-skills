package net.impleri.slab.world

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.world.level.biome.{Climate, FixedBiomeSource}
import net.minecraft.world.level.chunk.{ChunkAccess, LevelChunk}
import net.minecraft.world.level.lighting.LevelLightEngine

case class Chunk[T <: Chunk.BaseVanilla](underlying: T) {
  private[slab] def pos = underlying.getPos

  private[slab] def toUpdatePacket(lightEngine: LevelLightEngine): Option[ClientboundLevelChunkWithLightPacket] =
    underlying match {
      case c: Chunk.Vanilla => Option(new ClientboundLevelChunkWithLightPacket(c, lightEngine, null, null, true))
      case _ => None
    }

  private def handleUpdate(f: => Unit): Chunk[T] = {
    f
    underlying.setUnsaved(true)

    copy(underlying = underlying)
  }

  def setBiome(biome: Biome, climateSampler: Climate.Sampler): Chunk[T] =
    handleUpdate {
      underlying.fillBiomesFromNoise(
        new FixedBiomeSource(biome.toHolder),
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
