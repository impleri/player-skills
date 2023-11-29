package net.impleri.playerskills.client

object PlayerSkillsClient {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ClientStateContainer = ClientStateContainer(eventHandler = EVENTS)

  def init(): Unit = {
    STATE.getNetHandler.resyncPlayer()
  }
}
