package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.crafting.RecipeManager
import net.minecraft.core.{Registry => McRegistry}
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.Level

import java.nio.file.Path
import java.util.UUID
import scala.jdk.javaapi.CollectionConverters
import scala.jdk.javaapi.OptionConverters
import scala.util.chaining.scalaUtilChainingOps

class Server(private val server: MinecraftServer, private val level: Option[Level] = None) {
  private val levelResource = new LevelResource(PlayerSkills.MOD_ID)

  def getWorldPath(level: LevelResource = levelResource): Path = server.getWorldPath(level)

  def getPlayer(playerId: UUID): Option[Player[ServerPlayer]] = {
    server
      .getPlayerList
      .getPlayer(playerId)
      .pipe(Option.apply)
      .map(Player.apply)
  }

  def getPlayers: Seq[Player[ServerPlayer]] = {
    CollectionConverters.asScala(server.getPlayerList.getPlayers).toList
      .map(Player.apply)
  }

  def getLevel: Option[Level] = level

  def getDimensions: Seq[ResourceLocation] = {
    CollectionConverters.asScala(server.levelKeys())
      .map(_.location())
      .toList
  }

  def getRecipeManager: RecipeManager = server.getRecipeManager.pipe(crafting.RecipeManager(_))

  def getRegistry[T](key: ResourceKey[McRegistry[T]]): Option[Registry[T]] = {
    OptionConverters
      .toScala(server.registryAccess().registry[T](key))
      .map(r => Registry(r))
  }
}

object Server {
  def apply(server: MinecraftServer): Server = new Server(server)

  def apply(level: Level): Server = new Server(level.getServer, Option(level))
}
