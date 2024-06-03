package net.impleri.slab.server

import net.impleri.slab.entity.Player
import net.impleri.slab.item.crafting.RecipeManager
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.core.{Registry => McRegistry}
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.Level

import java.nio.file.Path
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.chaining.scalaUtilChainingOps

class Server(private val underlying: MinecraftServer, resourcePath: String, private val level: Option[Level] = None) {
  private val levelResource = new LevelResource(resourcePath)

  def getWorldPath(level: LevelResource = levelResource): Path = underlying.getWorldPath(level)

  def getPlayer(playerId: UUID): Option[Player[ServerPlayer]] = {
    underlying
      .getPlayerList
      .getPlayer(playerId)
      .pipe(Option.apply)
      .map(Player.apply)
  }

  def getPlayers: Seq[Player[ServerPlayer]] = {
    underlying.getPlayerList.getPlayers.asScala.toList
      .map(Player.apply)
  }

  def getLevel: Option[Level] = level

  def getDimensions: Seq[ResourceLocation] = {
    underlying.levelKeys().asScala
      .map(_.location())
      .toList
  }

  def getRecipeManager: RecipeManager = underlying.getRecipeManager.pipe(RecipeManager(_))

  def getRegistry[T <: ResourceWrapper[U], U](key: ResourceKey[McRegistry[U]], f: U => T): Option[Registry[T, U]] = {
    underlying.registryAccess()
      .registry[U](key)
      .toScala
      .map(r => new Registry(r, f))
  }
}

object Server {
  def apply(server: MinecraftServer, resourcePath: String = ""): Server = new Server(server, resourcePath)

  def fromLevel(level: Level, resourcePath: String = ""): Option[Server] = {
    Option(level)
      .map(l =>
        new Server(l.getServer,
          resourcePath,
          Option(l),
        ),
      )
  }
}
