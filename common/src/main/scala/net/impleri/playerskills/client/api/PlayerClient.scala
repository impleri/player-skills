package net.impleri.playerskills.client.api

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.server.api.Player

object PlayerClient {
  def can[T](skill: Skill[T], expectedValue: Option[T]): Boolean =
    SkillType.get(skill)
      .map(_.can(skill, expectedValue))
      .getOrElse(Player.DEFAULT_SKILL_RESPONSE)
}
