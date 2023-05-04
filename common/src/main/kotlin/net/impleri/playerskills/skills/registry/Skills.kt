package net.impleri.playerskills.skills.registry

import dev.architectury.registry.registries.Registrar
import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

object Skills {
  val REGISTRY_KEY = SkillResourceLocation.of("skills_registry")

  /**
   * Initial Registry used for pulling in what other mods register
   */
  private val INITIAL_REGISTRY: Registrar<Skill<*>> = Registries.get(PlayerSkills.MOD_ID)
    .builder<Skill<*>>(REGISTRY_KEY)
    .build()

  /**
   * GAME Registry
   */
  private val REGISTRY: MutableMap<ResourceLocation, Skill<*>> = HashMap()

  // Dummy method to ensure static elements are created
  fun buildRegistry() {
    if (INITIAL_REGISTRY.key().location() != REGISTRY_KEY) {
      PlayerSkills.LOGGER.warn("Skills registry is invalid.")
    }
  }

  /**
   * Resets the GAME registry to match the Initial registry
   */
  fun resync() {
    REGISTRY.clear()
    INITIAL_REGISTRY.entrySet()
      .forEach { REGISTRY[it.key.location()] = it.value }
  }

  fun entries(): List<Skill<*>> {
    return REGISTRY.values.toList()
  }

  fun has(name: ResourceLocation): Boolean {
    return REGISTRY.containsKey(name)
  }

  fun <T> has(skill: Skill<T>): Boolean {
    return has(skill.name)
  }

  fun <T> find(name: ResourceLocation): Skill<T>? {
    return REGISTRY[name]?.cast()
  }

  /**
   * Find a Skill by name or throw an error
   */
  @Throws(RegistryItemNotFound::class)
  fun <T> findOrThrow(name: ResourceLocation): Skill<T> {
    return find(name) ?: throw RegistryItemNotFound()
  }

  /**
   * Adds a Skill if it does not already exist
   */
  @Throws(RegistryItemAlreadyExists::class)
  fun <T> add(skill: Skill<T>): Boolean {
    if (has(skill)) {
      throw RegistryItemAlreadyExists()
    }

    return upsert(skill)
  }

  /**
   * Upserts a skill in the registry even if it already exists
   */
  fun <T> upsert(skill: Skill<T>): Boolean {
    REGISTRY[skill.name] = skill

    return has(skill)
  }

  /**
   * Removes a Skill if it exists
   */
  fun remove(name: ResourceLocation): Boolean {
    REGISTRY.remove(name)

    return !has(name)
  }

  /**
   * Removes a Skill if it exists
   */
  fun <T> remove(skill: Skill<T>): Boolean {
    return remove(skill.name)
  }
}
