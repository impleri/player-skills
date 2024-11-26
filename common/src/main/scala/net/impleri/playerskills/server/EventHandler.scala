package net.impleri.playerskills.server

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.slab.entity.Player
import net.impleri.slab.pubsub.EventEmitter

import java.util.function.Consumer

case class EventHandler(
  SKILL_CHANGED: EventEmitter[SkillChangedEvent[_]] = EventEmitter(),
) {
  def onSkillChanged(listener: Consumer[SkillChangedEvent[_]]): Unit =
    SKILL_CHANGED.register(listener)

  def emitSkillChanged[T](
    player: Player,
    newSkill: Skill[T],
    oldSkill: Option[Skill[T]],
  ): Unit =
    SKILL_CHANGED.emit(SkillChangedEvent[T](player, Option(newSkill), oldSkill))
}
