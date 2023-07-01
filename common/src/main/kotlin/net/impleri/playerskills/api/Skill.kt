package net.impleri.playerskills.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.skills.registry.RegistryItemNotFound
import net.impleri.playerskills.skills.registry.Skills
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * Base generic skill. These are meant to be nothing more than
 * containers for data. All logic should be handled by the SkillType.
 */
abstract class Skill<T>(
  val name: ResourceLocation,
  val type: ResourceLocation,
  val value: T? = null,
  val description: String? = null,
  val options: List<T> = ArrayList(),
  val changesAllowed: Int = UNLIMITED_CHANGES,
  val teamMode: TeamMode = TeamMode.off(),
  val notify: Boolean = false,
  val notifyKey: String? = null,
) {
  abstract fun copy(value: T?, changesAllowed: Int): Skill<T>

  protected open fun getDefaultNotification(): Component {
    return formatNotification("playerskills.notify.skill_change")
  }

  fun areChangesAllowed(): Boolean {
    return changesAllowed != 0
  }

  fun copy(): Skill<T> {
    return copy(value, changesAllowed)
  }

  fun change(newValue: T?): Skill<T> {
    return if (areChangesAllowed()) copy(newValue, changesAllowed - 1) else copy()
  }

  @JvmOverloads
  fun getNotification(oldValue: T? = null): Component? {
    if (!notify || value == null) {
      return null
    }

    return if (notifyKey == null) getDefaultNotification() else formatNotification(notifyKey, oldValue)
  }

  @JvmOverloads
  protected fun formatNotification(messageKey: String, oldValue: T? = null): Component {
    val skillName = formatSkillName()
    val skillValue = formatSkillValue()
    val oldSkillValue = if (oldValue == null) Component.literal("") else formatSkillValue(oldValue)

    return Component.translatable(messageKey, skillName, skillValue, oldSkillValue)
  }

  protected fun formatSkillName(): Component {
    return Component.literal(name.path.replace("_", " "))
      .withStyle(ChatFormatting.DARK_AQUA)
      .withStyle(ChatFormatting.BOLD)
  }

  @JvmOverloads
  protected fun formatSkillValue(value: T? = null): Component {
    return Component.literal(value.toString())
      .withStyle(ChatFormatting.GOLD)
  }

  fun isAllowedValue(nextVal: T?): Boolean {
    return nextVal == null || options.isEmpty() || options.contains(nextVal)
  }

  @Suppress("UNCHECKED_CAST")
  fun <V> cast(): Skill<V> {
    return this as Skill<V>
  }

  private fun isSameAs(that: Skill<*>): Boolean {
    return that.name == this.name
  }

  private fun isSameType(that: Skill<*>): Boolean {
    return that.type == this.type
  }

  private fun <V> canEquals(that: Skill<V>): Boolean {
    return that::class.isInstance(this)
  }

  override fun equals(other: Any?): Boolean {
    if (other is Skill<*>) {
      return other.canEquals(this) && isSameAs(other) && isSameType(other)
    }

    return false
  }

  override fun hashCode(): Int {
    return name.hashCode() * type.hashCode()
  }

  companion object {
    const val UNLIMITED_CHANGES = -1

    @JvmField
    val REGISTRY_KEY = Skills.REGISTRY_KEY

    fun all(): List<Skill<*>> {
      return Skills.entries()
    }

    fun <T> modify(skill: Skill<T>): Boolean {
      PlayerSkills.LOGGER.info("Saving skill ${skill.name}")
      return Skills.upsert(skill)
    }

    fun <T> remove(skill: Skill<T>): Boolean {
      return Skills.remove(skill)
    }

    /**
     * Find a Skill by string
     */
    @Throws(RegistryItemNotFound::class)
    fun <V> findOrThrow(name: String): Skill<V> {
      return findOrThrow(SkillResourceLocation.of(name))
    }

    /**
     * Find a Skill by name
     */
    @Throws(RegistryItemNotFound::class)
    fun <V> findOrThrow(location: ResourceLocation): Skill<V> {
      return Skills.findOrThrow(location)
    }

    fun <V> find(location: ResourceLocation): Skill<V>? {
      return Skills.find(location)
    }

    fun <V> find(location: String): Skill<V>? {
      return find(SkillResourceLocation.of(location))
    }
  }
}
