package net.impleri.playerskills.events

import dev.architectury.event.Event
import dev.architectury.event.EventFactory
import net.impleri.playerskills.api.skills.Skill
import net.minecraft.world.entity.player.Player

import java.util.function.Consumer

case class SkillChangedEvent[T](
  player: Player,
  next: Option[Skill[T]],
  previous: Option[Skill[T]],
)

object SkillChangedEvent {
  val EVENT: Event[Consumer[SkillChangedEvent[_]]] = EventFactory.createConsumerLoop()
}
