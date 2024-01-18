package net.impleri.playerskills.skills

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.architectury.Registrar
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.StatefulRegistry

import scala.util.chaining.scalaUtilChainingOps

/**
 * In-game registry interfacing for Skills
 */
class SkillRegistry(
  override var state: SkillRegistryState.Skills,
  private val gameRegistrar: Registrar[Skill[_]],
) extends StatefulRegistry[SkillRegistryState.Skills] {
  /**
   * Helper method to aggregate the skills created directly by mods
   */
  private def getInitialSkills = {
    gameRegistrar.entries().values.toList
  }

  /**
   * Resets the state to match the initially created skills
   */
  def resync(): Unit = {
    SkillRegistryState.resync(getInitialSkills)
      .pipe(maintainState)
  }

  /**
   * Get all skills in the state
   */
  def entries: List[Skill[_]] = {
    SkillRegistryState.entries().pipe(maintainState)
  }

  /**
   * Checks if there is a skill saved in state with the given name
   */
  def has(key: ResourceLocation): Boolean = {
    SkillRegistryState.has(key).pipe(maintainState)
  }

  def has(value: Skill[_]): Boolean = has(value.name)

  /**
   * Returns the full skill if one exists in state with the given name
   */
  def find(key: ResourceLocation): Option[Skill[_]] = {
    SkillRegistryState.find(key).pipe(maintainState)
  }

  /**
   * Upsert a skill in state even if it already exists
   */
  def upsert(skill: Skill[_]): Unit = {
    SkillRegistryState.upsert(skill)
      .pipe(maintainState)
  }

  /**
   * Adds a Skill if it does not already exist
   */
  def add(skill: Skill[_]): Boolean = {
    SkillRegistryState.add(skill)
      .run(state).map(r => {
        state = r._1
      },
      ).isRight
  }

  /**
   * Removes a Skill if it exists
   */
  def remove(key: ResourceLocation): Unit = {
    SkillRegistryState.remove(key)
      .pipe(maintainState)
  }

  def removeSkill(skill: Skill[_]): Unit = remove(skill.name)
}

object SkillRegistry {
  val REGISTRY_KEY: ResourceLocation = ResourceLocation("skills_registry").get

  lazy val REGISTRAR: Registrar[Skill[_]] = Registrar(REGISTRY_KEY.name)

  def apply(
    state: SkillRegistryState.Skills = SkillRegistryState.empty,
    gameRegistrar: Registrar[Skill[_]] = Registrar(None),
  ): SkillRegistry = {
    new SkillRegistry(state, gameRegistrar)
  }
}
