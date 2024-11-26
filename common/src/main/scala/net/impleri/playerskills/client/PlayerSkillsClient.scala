package net.impleri.playerskills.client

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.client.bindings.ClientEventBindings
import net.impleri.slab.client.Client
import net.impleri.slab.resources.ResourceManager

import scala.annotation.unused

object PlayerSkillsClient {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ClientStateContainer =
    ClientStateContainer(PlayerSkills.STATE, EVENTS)

  private val EVENT_BINDINGS: ClientEventBindings =
    ClientEventBindings(onReload)

  def init(): Unit =
    EVENT_BINDINGS.registerEvents()

  private def onReload(
    @unused resourceManager: Option[ResourceManager],
  ): Unit =
    Client()
      .getPlayer
      .foreach(STATE.getNetHandler.resyncPlayer)
}
