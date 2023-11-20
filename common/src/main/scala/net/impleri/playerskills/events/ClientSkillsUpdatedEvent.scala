package net.impleri.playerskills.events

import net.impleri.playerskills.api.skills.Skill

case class ClientSkillsUpdatedEvent(
  next: List[Skill[_]],
  prev: List[Skill[_]],
  forced: Boolean,
)
