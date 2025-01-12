package net.impleri.slab.world

import net.impleri.slab.block.Block
import net.impleri.slab.entity.Player
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.server.level.{ServerChunkCache, ServerLevel}
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.biome.Climate

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.Try

case class Level[T <: Level.BaseVanilla](private val underlying: T) {
  private def asFull: Option[Level.Vanilla] =
    Option(underlying) match {
      case Some(l: Level.Vanilla) => Option(l)
      case _ => None
    }

  private def ifServer: Option[Level.VanillaServer] =
    Option(underlying) match {
      case Some(l: Level.VanillaServer) => Option(l)
      case _ => None
    }

  private def getDimension: Option[ResourceKey[Level.Vanilla]] = asFull
    .map(_.dimension())
    .map(ResourceKey(_))

  def getDimensionName: Option[ResourceLocation] = getDimension.flatMap(_.name)

  def getBiome(name: ResourceLocation): Option[Biome] =
    ifServer.flatMap { sl =>
    sl
      .registryAccess()
      .registry(ResourceKey.BIOME_REGISTRY.value)
      .toScala
      .map(_.getHolder(ResourceKey.forResource[Biome.Vanilla, Registry.Vanilla[Biome.Vanilla]](name, ResourceKey.BIOME_REGISTRY).value))
      .flatMap(_.toScala)
      .map(Biome(_))
  }

  private[slab] def getServerChunkSource: Option[ServerChunkCache] =
    ifServer.map(_.getChunkSource.asInstanceOf[ServerChunkCache])

  private[slab] def getClimateSampler: Option[Climate.Sampler] =
    getServerChunkSource.map(_.randomState().sampler())

  def getPlayersInRange(chunk: Chunk[_]): Seq[Player] =
    getServerChunkSource.map(_.chunkMap)
      .toSeq
      .flatMap(_.getPlayers(chunk.pos, false).asScala)
      .map(Player(_))


  def save(): Boolean =
    getServerChunkSource
      .map(_.save(false))
      .fold(false)(_ => true)

  def getChunk(pos: Position): Option[Chunk.Any] =
    Try(underlying.getChunk(pos.value))
      .toOption
      .map(Chunk(_))

  def updateChunk(chunk: Chunk[_]): Unit = {
    underlying.getChunkSource.updateChunkForced(chunk.pos, true)
    val players = getPlayersInRange(chunk)

    chunk.toUpdatePacket(underlying.getLightEngine)
      .foreach(packet => players.foreach(_.sendPacket(packet)))
  }

  def getBiome(pos: Position): Option[Biome] =
    Try(underlying.getBiome(pos.value))
      .toOption
      .map(Biome(_))

  def getBlockAt(pos: Position): Option[Block] =
    Option(underlying.getBlockState(pos.value)).map(Block(_))
}

object Level {
  type Any = Level[_]

  type BaseVanilla = LevelAccessor

  type Vanilla = McLevel

  type VanillaServer = ServerLevel
}
