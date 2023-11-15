package net.impleri.playerskills.client.api

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps

case class ClientPlayer(
  skillTypeOps: SkillTypeOps = SkillType(),
) {
  def can[T](skill: Skill[T], expectedValue: Option[T] = None): Boolean = {
    skillTypeOps.get(skill).fold(ClientPlayer.DEFAULT_SKILL_RESPONSE)(_.can(skill, expectedValue))
  }
}

object ClientPlayer {
  val DEFAULT_SKILL_RESPONSE: Boolean = true
}
