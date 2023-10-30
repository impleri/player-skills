package net.impleri.playerskills.facades

import net.impleri.playerskills.PlayerSkills
import net.minecraft.server.{MinecraftServer => Server}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelResource

import java.nio.file.Path
import java.util.UUID
import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

class MinecraftServer(private val server: Server) {
  private val levelResource = new LevelResource(PlayerSkills.MOD_ID)

  def getWorldPath(level: LevelResource = levelResource): Path = server.getWorldPath(level)

  def getPlayer(playerId: UUID): Option[MinecraftPlayer[ServerPlayer]] = {
    server
      .getPlayerList
      .getPlayer(playerId)
      .pipe(Option.apply)
      .map(MinecraftPlayer.apply)
  }

  def getPlayers: List[MinecraftPlayer[ServerPlayer]] = {
    CollectionConverters.asScala(server.getPlayerList.getPlayers).toList
      .map(MinecraftPlayer.apply)
  }
}

object MinecraftServer {
  def apply(server: Server): MinecraftServer = new MinecraftServer(server)
}
