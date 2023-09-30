package net.impleri.playerskills.events

import dev.architectury.event.Event
import dev.architectury.event.EventFactory
import net.impleri.playerskills.api.skills.Skill

import java.util.function.Consumer

case class ClientSkillsUpdatedEvent(
  next: List[Skill[_]],
  prev: List[Skill[_]],
  forced: Boolean,
)

object ClientSkillsUpdatedEvent {
  val EVENT: Event[Consumer[ClientSkillsUpdatedEvent]] = EventFactory.createConsumerLoop()
}
