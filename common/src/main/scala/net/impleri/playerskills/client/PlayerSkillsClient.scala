package net.impleri.playerskills.client

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.client.bindings.InternalEvents
import net.impleri.playerskills.facades.minecraft.Client
import net.minecraft.server.packs.resources.ResourceManager

object PlayerSkillsClient {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ClientStateContainer = ClientStateContainer(PlayerSkills.STATE, EVENTS)

  private val INTERNAL: InternalEvents = InternalEvents(onReload)

  def init(): Unit = {
    INTERNAL.registerEvents()
  }

  private def onReload(resourceManager: ResourceManager): Unit = {
    Client().getPlayer.foreach(STATE.getNetHandler.resyncPlayer)
  }
}
