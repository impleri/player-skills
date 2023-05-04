package net.impleri.playerskills.skills.registry

import dev.architectury.registry.registries.Registries
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

object SkillTypes {
  val REGISTRY_KEY = SkillResourceLocation.of("skill_types_registry")

  private val REGISTRY = Registries.get(PlayerSkills.MOD_ID)
    .builder<SkillType<*>>(REGISTRY_KEY)
    .build()

  // Dummy method to ensure static elements are created
  fun buildRegistry() {
    if (REGISTRY.key().location() != REGISTRY_KEY) {
      PlayerSkills.LOGGER.warn("Skills registry is invalid.")
    }
  }

  /**
   * Get all SkillTypes registered
   */
  fun entries(): List<SkillType<*>> {
    return REGISTRY.entrySet().map { it.value }.toList()
  }

  fun <T> find(name: ResourceLocation): SkillType<T>? {
    return REGISTRY[name]?.cast()
  }

  /**
   * Find a SkillType by name or throw an error
   */
  @Throws(RegistryItemNotFound::class)
  fun <T> findOrThrow(name: ResourceLocation): SkillType<T> {
    return find(name) ?: throw RegistryItemNotFound()
  }
}
