package net.impleri.playerskills.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.skills.registry.RegistryItemNotFound
import net.impleri.playerskills.skills.registry.SkillTypes
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

/**
 * Wrapper to `Skill`s providing logic for
 * 1. serialization to/from NBT
 * 2. Executing logic to determine if a skill value should be changed.
 */
abstract class SkillType<T> {
  open val name: ResourceLocation
    get() = SkillResourceLocation.of("skill")

  protected abstract fun castToString(value: T?): String?
  internal abstract fun castFromString(value: String?): T?
  abstract fun getPrevValue(skill: Skill<T>, min: T? = null, max: T? = null): T

  abstract fun getNextValue(skill: Skill<T>, min: T? = null, max: T? = null): T

  /**
   * Logic to determine if a player has a skill at an expected level
   */
  @JvmOverloads
  open fun can(skill: Skill<T>, expectedValue: T? = null): Boolean {
    return if (expectedValue == null) {
      skill.value != null
    } else {
      skill.value === expectedValue
    }
  }

  /**
   * Convert into string for NBT storage
   */
  private fun serialize(skill: Skill<T>): String {
    val value = castToString(skill.value)

    return arrayOf(
      skill.name.toString(),
      skill.type.toString(),
      if (value?.isEmpty() != false) stringValueNone else value,
      skill.changesAllowed.toString(),
    ).joinToString(valueSeparator)
  }

  /**
   * Convert from string in NBT storage
   */
  @Throws(RegistryItemNotFound::class)
  private fun unserialize(skillName: String, value: String?, changesAllowed: Int): Skill<T>? {
    val name = SkillResourceLocation.of(skillName)
    val castValue = castFromString(value)

    return Skill.find<T>(name)?.copy(castValue, changesAllowed)
  }

  @Suppress("UNCHECKED_CAST")
  fun <V> cast(): SkillType<V> {
    return this as SkillType<V>
  }

  companion object {
    private const val valueSeparator = ";"
    private const val stringValueNone = "[NULL]"

    val REGISTRY_KEY = SkillTypes.REGISTRY_KEY

    /**
     * Get all Types
     */
    fun all(): List<SkillType<*>> {
      return SkillTypes.entries()
    }

    /**
     * Find a SkillType
     */
    @Throws(RegistryItemNotFound::class)
    fun <V> findOrThrow(name: String): SkillType<V> {
      return findOrThrow(SkillResourceLocation.of(name))
    }

    @Throws(RegistryItemNotFound::class)
    fun <V> findOrThrow(location: ResourceLocation): SkillType<V> {
      return SkillTypes.findOrThrow(location)
    }

    /**
     * Find a SkillType
     */
    fun <V> find(skill: ResourceLocation): SkillType<V>? {
      return SkillTypes.find(skill)
    }

    fun <V> find(skill: Skill<V>): SkillType<V>? {
      return find(skill.type)
    }

    @ApiStatus.Internal
    fun <V> serializeToString(skill: Skill<V>): String {
      val storage = find(skill)?.serialize(skill) ?: ""

      PlayerSkills.LOGGER.debug("Dehydrated skill ${skill.name} of type ${skill.type} for storage: $storage")

      return storage
    }

    private data class SerialParts(val name: String, val type: String, val value: String?, val changesAllowed: String)

    @ApiStatus.Internal
    internal fun unserializeFromString(rawSkill: String?): Skill<*>? {
      if (rawSkill?.isEmpty() != false) {
        PlayerSkills.LOGGER.warn("Unable to unpack skill $rawSkill from storage")
        return null
      }

      val (name, type, value, changesAllowed) = splitRawSkill(rawSkill)

      val remainingChanges: Int = try {
        changesAllowed.toInt()
      } catch (e: NumberFormatException) {
        PlayerSkills.LOGGER.error(
          "Unable to parse changesAllowed ($changesAllowed) back into an integer, data possibly corrupted",
        )

        return null
      }

      PlayerSkills.LOGGER.debug("Hydrating $type skill named $name: $value")
      return find<Any>(SkillResourceLocation.of(type))?.unserialize(name, value, remainingChanges)
    }

    private fun splitRawSkill(value: String): SerialParts {
      val parts = value.split(valueSeparator.toRegex())
        .dropLastWhile { it.isEmpty() }
        .toTypedArray()

      val skillValue = if (parts[2] == stringValueNone) null else parts[2]

      return SerialParts(parts[0], parts[1], skillValue, parts[3])
    }
  }
}
