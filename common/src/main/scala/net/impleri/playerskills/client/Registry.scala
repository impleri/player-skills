package net.impleri.playerskills.client

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.collection.mutable
import scala.util.chaining._

object Registry {
  private val playerSkills: mutable.ListBuffer[Skill[_]] = mutable.ListBuffer()

  def get: List[Skill[_]] = playerSkills.toList

  def syncFromServer(skills: List[Skill[_]], force: Boolean): Unit =
    get
      .tap(_ => playerSkills.clear())
      .tap(_ => PlayerSkillsLogger.SKILLS.info(s"Syncing Client-side skills: ${skills.map(s => s"(${s.name}=${s.value.getOrElse("None")})").mkString(", ")}"))
      .tap(_ => playerSkills.addAll(skills))
      .pipe(PlayerSkillsClient.emitSkillsUpdated(skills, _, force))
}
