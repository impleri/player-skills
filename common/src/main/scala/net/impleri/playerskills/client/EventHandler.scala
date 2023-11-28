package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.facades.architectury.EventEmitter

import java.util.function.Consumer

case class EventHandler(private val CLIENT_SKILLS_UPDATED: EventEmitter[ClientSkillsUpdatedEvent] = EventEmitter()) {
  def onSkillsUpdate(listener: Consumer[ClientSkillsUpdatedEvent]): Unit = {
    CLIENT_SKILLS_UPDATED.register(listener)
  }

  def emitSkillsUpdated(next: List[Skill[_]], prev: List[Skill[_]], force: Boolean): Unit = {
    CLIENT_SKILLS_UPDATED.emit(ClientSkillsUpdatedEvent(next, prev, force))
  }
}
