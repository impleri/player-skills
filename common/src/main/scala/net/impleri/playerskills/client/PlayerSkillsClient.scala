package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent

object PlayerSkillsClient {
  val STATE: ClientStateContainer = ClientStateContainer()

  def init(): Unit = {}

  def resyncSkills(): Unit = STATE.getNetHandler.resyncPlayer()

  def syncFromServer(skills: List[Skill[_]], force: Boolean): Unit = STATE.SKILLS.syncFromServer(skills, force)

  def emitSkillsUpdated(next: List[Skill[_]], prev: List[Skill[_]], force: Boolean): Unit = {
    ClientSkillsUpdatedEvent.EVENT.invoker().accept(ClientSkillsUpdatedEvent(next, prev, force))
  }
}
