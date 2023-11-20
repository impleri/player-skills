package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.PlayerSkills
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource

import java.nio.file.Path
import java.util.UUID
import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

class Server(private val server: MinecraftServer) {
  private val levelResource = new LevelResource(PlayerSkills.MOD_ID)

  def getWorldPath(level: LevelResource = levelResource): Path = server.getWorldPath(level)

  def getPlayer(playerId: UUID): Option[Player[ServerPlayer]] = {
    server
      .getPlayerList
      .getPlayer(playerId)
      .pipe(Option.apply)
      .map(Player.apply)
  }

  def getPlayers: List[Player[ServerPlayer]] = {
    CollectionConverters.asScala(server.getPlayerList.getPlayers).toList
      .map(Player.apply)
  }
}

object Server {
  def apply(server: MinecraftServer): Server = new Server(server)
}
