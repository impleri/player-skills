package net.impleri.playerskills.client

import net.impleri.playerskills.PlayerSkills

object PlayerSkillsClient {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ClientStateContainer = ClientStateContainer(PlayerSkills.STATE, EVENTS)

  def init(): Unit = {
    STATE.getNetHandler.resyncPlayer()
  }
}
