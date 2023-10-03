package net.impleri.playerskills.events.handlers

import net.minecraft.server.MinecraftServer

import java.util.UUID

object EventHandlers {
  private[handlers] var server: Option[MinecraftServer] = None

  private val INTERNAL = InternalEvents()
  private val COMMAND = CommandEvents()
  private val LIFECYCLE = LifecycleEvents(server = _)

  def init(): Unit = {
    LIFECYCLE.registerEvents()
    COMMAND.registerEvents()
    INTERNAL.registerEvents()
  }

  def resync(playerId: UUID): Unit = INTERNAL.resyncPlayer(server, playerId)

  lazy val withServer: MinecraftServer = server.get
}
