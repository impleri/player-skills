package net.impleri.playerskills.skills

import dev.architectury.registry.registries.Registrar
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.utils.StatefulRegistry
import net.minecraft.resources.ResourceLocation

import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

/**
 * In-game registry interfacing for Skills
 */
case class SkillRegistry(
  override var state: SkillRegistryState.Skills,
  private val gameRegistrar: Registrar[Skill[_]],
) extends StatefulRegistry[SkillRegistryState.Skills] {
  /**
   * Helper method to aggregate the skills created directly by mods
   */
  private def getInitialSkills = CollectionConverters.asScala(gameRegistrar
    .entrySet())
    .map(_.getValue)
    .pipe(_.toList)

  /**
   * Resets the state to match the initially created skills
   */
  def resync(): Unit =
    SkillRegistryState.resync(getInitialSkills)
      .pipe(maintainState)

  /**
   * Get all skills in the state
   */
  def entries: List[Skill[_]] = SkillRegistryState.entries().pipe(maintainState)

  /**
   * Checks if there is a skill saved in state with the given name
   */
  def has(key: ResourceLocation): Boolean = SkillRegistryState.has(key).pipe(maintainState)

  def has(value: Skill[_]): Boolean = has(value.name)

  /**
   * Returns the full skill if one exists in state with the given name
   */
  def find(key: ResourceLocation): Option[Skill[_]] = SkillRegistryState.find(key).pipe(maintainState)

  /**
   * Upsert a skill in state even if it already exists
   */
  def upsert(skill: Skill[_]): Unit =
    SkillRegistryState.upsert(skill)
      .pipe(maintainState)

  /**
   * Adds a Skill if it does not already exist
   */
  def add(skill: Skill[_]): Boolean =
    SkillRegistryState.add(skill)
      .run(state).map(r => {
        state = r._1
      }).isRight

  /**
   * Removes a Skill if it exists
   */
  def remove(key: ResourceLocation): Unit = SkillRegistryState.remove(key)
    .pipe(maintainState)

  def remove(skill: Skill[_]): Unit = remove(skill.name)
}

object SkillRegistry {
  val REGISTRY_KEY: ResourceLocation = SkillResourceLocation.of("skills_registry").get

  /**
   * Architectury Registry used for pulling in skills registered by  other mods
   */
  private lazy val INITIAL_REGISTRY: Registrar[Skill[_]] = Registries.get(PlayerSkills.MOD_ID)
    .builder(REGISTRY_KEY)
    .build()

  def apply(
    state: SkillRegistryState.Skills = SkillRegistryState.empty,
    gameRegistrar: Registrar[Skill[_]] = INITIAL_REGISTRY
  ): SkillRegistry = new SkillRegistry(state, gameRegistrar)
}
