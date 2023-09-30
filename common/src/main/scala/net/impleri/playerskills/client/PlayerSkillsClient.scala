package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent

object PlayerSkillsClient {
  def init() : Unit = {}

  def resyncSkills(): Unit = NetHandler.resyncPlayer()

  def syncFromServer(skills: List[Skill[_]], force: Boolean): Unit = Registry.syncFromServer(skills, force)

  def emitSkillsUpdated(next: List[Skill[_]], prev: List[Skill[_]], force: Boolean): Unit =
    ClientSkillsUpdatedEvent.EVENT.invoker().accept(ClientSkillsUpdatedEvent(next, prev, force))
}
