package net.impleri.playerskills.events

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.minecraft.Player

case class SkillChangedEvent[T](
  player: Player[_],
  next: Option[Skill[T]],
  previous: Option[Skill[T]],
)
