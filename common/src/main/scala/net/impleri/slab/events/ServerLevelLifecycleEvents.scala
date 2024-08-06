package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.LifecycleEvent.ServerLevelState
import net.impleri.slab.world.Level
import net.minecraft.server.level.ServerLevel

import scala.util.chaining.scalaUtilChainingOps

case class ServerLevelLifecycleEvents(
  private val onLevelLoadEvent: Event[ServerLevelState] =
    LifecycleEvent.SERVER_LEVEL_LOAD,
  private val onLevelUnloadEvent: Event[ServerLevelState] =
    LifecycleEvent.SERVER_LEVEL_UNLOAD,
  private val onLevelSaveEvent: Event[ServerLevelState] =
    LifecycleEvent.SERVER_LEVEL_SAVE,
) {
  private def handleEvent(
    server: ServerLevel,
    f: ServerLevelLifecycleEvents.OnLevelLifecycle,
  ): Unit = {
    Option(server).map(Level(_)).pipe(f)
  }

  def onLevelLoad(
    handler: ServerLevelLifecycleEvents.OnLevelLifecycle,
  ): Unit = {
    onLevelLoadEvent.register(handleEvent(_, handler))
  }

  def onLevelUnload(
    handler: ServerLevelLifecycleEvents.OnLevelLifecycle,
  ): Unit = {
    onLevelUnloadEvent.register(handleEvent(_, handler))
  }

  def onLevelSave(
    handler: ServerLevelLifecycleEvents.OnLevelLifecycle,
  ): Unit = {
    onLevelSaveEvent.register(handleEvent(_, handler))
  }
}

object ServerLevelLifecycleEvents {
  type OnLevelLifecycle = Option[Level[_]] => Unit
}
