package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill

object PlayerSkillsClient {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ClientStateContainer = ClientStateContainer(eventHandler = EVENTS)


  // TODO: Move from here
  def resyncSkills(): Unit = {
    STATE.getNetHandler.resyncPlayer()
  }

  def syncFromServer(skills: List[Skill[_]], force: Boolean): Unit = STATE.SKILLS.syncFromServer(skills, force)
}
