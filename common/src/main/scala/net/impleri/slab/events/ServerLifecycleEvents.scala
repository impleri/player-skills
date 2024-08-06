package net.impleri.slab.events

import dev.architectury.event.events.common.LifecycleEvent.ServerState
import dev.architectury.event.Event
import dev.architectury.event.events.common.LifecycleEvent
import net.impleri.slab.server.Server
import net.minecraft.server.MinecraftServer

import scala.util.chaining.scalaUtilChainingOps

case class ServerLifecycleEvents(
  private val beforeServerStartEvent: Event[ServerState] =
    LifecycleEvent.SERVER_BEFORE_START,
  private val onServerStartEvent: Event[ServerState] =
    LifecycleEvent.SERVER_STARTING,
  private val afterServerStartedEvent: Event[ServerState] =
    LifecycleEvent.SERVER_STARTED,
  private val beforeServerStopEvent: Event[ServerState] =
    LifecycleEvent.SERVER_STOPPING,
  private val afterServerStoppedEvent: Event[ServerState] =
    LifecycleEvent.SERVER_STOPPED,
) {
  private def handleEvent(
    server: MinecraftServer,
    f: ServerLifecycleEvents.OnServerLifecycle,
  ): Unit = {
    Option(server).map(Server(_)).pipe(f)
  }

  def beforeServerStart(
    handler: ServerLifecycleEvents.OnServerLifecycle,
  ): Unit = {
    beforeServerStartEvent.register(handleEvent(_, handler))
  }

  def onServerStart(handler: ServerLifecycleEvents.OnServerLifecycle): Unit = {
    onServerStartEvent.register(handleEvent(_, handler))
  }

  def afterServerStarted(
    handler: ServerLifecycleEvents.OnServerLifecycle,
  ): Unit = {
    afterServerStartedEvent.register(handleEvent(_, handler))
  }

  def beforeServerStop(
    handler: ServerLifecycleEvents.OnServerLifecycle,
  ): Unit = {
    beforeServerStopEvent.register(handleEvent(_, handler))
  }

  def afterServerStopped(
    handler: ServerLifecycleEvents.OnServerLifecycle,
  ): Unit = {
    afterServerStoppedEvent.register(handleEvent(_, handler))
  }
}

object ServerLifecycleEvents {
  type OnServerLifecycle = Option[Server] => Unit
}
