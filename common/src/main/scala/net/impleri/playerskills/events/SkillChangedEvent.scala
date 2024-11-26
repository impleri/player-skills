package net.impleri.playerskills.events

import net.impleri.playerskills.api.skills.Skill
import net.impleri.slab.entity.Player

case class SkillChangedEvent[T](
  player: Player,
  next: Option[Skill[T]],
  previous: Option[Skill[T]],
)
