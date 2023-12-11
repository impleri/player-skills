package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.{Server => ServerFacade}
import net.impleri.playerskills.server.skills.PlayerRegistry

import scala.util.chaining.scalaUtilChainingOps

case class LifecycleEvents(
  playerRegistry: PlayerRegistry,
  onSetup: () => Unit = () => {},
  onServerChange: Option[ServerFacade] => Unit = _ => {},
  setupEvent: Event[Runnable] = LifecycleEvent.SETUP,
  beforeStartEvent: Event[LifecycleEvent.ServerState] = LifecycleEvent.SERVER_BEFORE_START,
  onStartEvent: Event[LifecycleEvent.ServerState] = LifecycleEvent.SERVER_STARTED,
  onStopEvent: Event[LifecycleEvent.ServerState] = LifecycleEvent.SERVER_STOPPING,
) {
  private[server] def registerEvents(): Unit = {
    setupEvent.register(() => setup())
    beforeStartEvent.register(ServerFacade(_).pipe(beforeServerStart))
    onStartEvent.register(_ => onServerStart())
    onStopEvent.register(_ => beforeServerStops())
  }

  private[bindings] def setup(): Unit = {
    onSetup()
  }

  private[bindings] def beforeServerStart(server: ServerFacade): Unit = {
    onServerChange(Option(server))
  }

  private[bindings] def onServerStart(): Unit = {
    //    MobRestrictionBuilder.register()
    //    ItemRestrictionBuilder.register()
  }

  private[bindings] def beforeServerStops(): Unit = {
    playerRegistry.close()
    onServerChange(None)
  }
}
