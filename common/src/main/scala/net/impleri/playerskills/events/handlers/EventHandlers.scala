package net.impleri.playerskills.events.handlers

import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.resources.ResourceManager

import java.util.UUID

case class EventHandlers(
  onSetup: () => Unit,
  onServerChange: Option[MinecraftServer] => Unit,
  onReloadResources: ResourceManager => Unit
) {
  private val INTERNAL = InternalEvents(onReloadResources)
  private val COMMAND = CommandEvents()
  private val LIFECYCLE = LifecycleEvents(onSetup, onServerChange)

  def init(): Unit = {
    LIFECYCLE.registerEvents()
    COMMAND.registerEvents()
    INTERNAL.registerEvents()
  }

  def resync(playerId: UUID, server: MinecraftServer): Unit = INTERNAL.resyncPlayer(server, playerId)
}


object EventHandlers {
  private def noOp = () => ()

  private def noOp1[T] = (_: T) => ()

  def apply(
    onSetup: () => Unit = noOp,
    onServerChange: Option[MinecraftServer] => Unit = noOp1,
    onReloadResources: ResourceManager => Unit = noOp1,
  ) = new EventHandlers(onSetup, onServerChange, onReloadResources)
}
