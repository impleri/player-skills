package net.impleri.playerskills.events

import dev.architectury.event.Event
import dev.architectury.event.EventFactory
import net.impleri.playerskills.api.Skill
import java.util.function.Consumer

@JvmRecord
data class ClientSkillsUpdatedEvent(
  val next: List<Skill<*>>,
  val prev: List<Skill<*>>,
  val forced: Boolean,
) {
  companion object {
    val EVENT: Event<Consumer<ClientSkillsUpdatedEvent>> = EventFactory.createConsumerLoop()
  }
}
