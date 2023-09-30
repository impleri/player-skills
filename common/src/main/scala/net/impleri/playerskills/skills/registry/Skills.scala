package net.impleri.playerskills.skills.registry

import dev.architectury.registry.registries.Registrar
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

import scala.collection.mutable
import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

object Skills {
  val REGISTRY_KEY: ResourceLocation = SkillResourceLocation.of("skills_registry").get

  /**
   * Initial Registry used for pulling in what other mods register
   */
  private val INITIAL_REGISTRY: Registrar[Skill[_]] = Registries.get(PlayerSkills.MOD_ID)
    .builder(REGISTRY_KEY)
    .build()

  /**
   * GAME Registry used to track final Skills after modifications from scripts
   */
  private val REGISTRY: mutable.Map[ResourceLocation, Skill[_]] = mutable.HashMap()

  private[playerskills] def init(): Unit = ()

  def resync(): Unit =
    REGISTRY.clear()
      .pipe(_ => INITIAL_REGISTRY.entrySet())
      .pipe(e => CollectionConverters.asScala(e))
      .map(t => (t.getKey.location(), t.getValue))
      .pipe(REGISTRY.addAll)

  /**
   * Resets the GAME registry to match the Initial registry
   */
  def reset(): Unit = resync()

  def entries: List[Skill[_]] = REGISTRY.values.toList

  def has(key: ResourceLocation): Boolean = REGISTRY.contains(key)

  def has(value: Skill[_]): Boolean = has(value.name)

  def find(key: ResourceLocation): Option[Skill[_]] = REGISTRY.get(key)

  /**
   * Upsert a skill in the registry even if it already exists
   */
  def upsert(skill: Skill[_]): Boolean =
    REGISTRY.addOne(skill.name, skill)
      .pipe(_ => has(skill))

  /**
   * Adds a Skill if it does not already exist
   */
  def add(skill: Skill[_]): Option[Boolean] =
    if (has(skill)) None else Option(upsert(skill))

  /**
   * Removes a Skill if it exists
   */
  def remove(key: ResourceLocation): Boolean = REGISTRY.remove(key).nonEmpty


  /**
   * Removes a Skill if it exists
   */
  def remove(skill: Skill[_]): Boolean = remove(skill.name)
}
