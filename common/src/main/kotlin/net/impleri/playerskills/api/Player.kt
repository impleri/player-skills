package net.impleri.playerskills.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.RegistryItemNotFound
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import java.util.UUID

object Player {
  fun get(player: UUID): List<Skill<*>> {
    return Players.get(player)
  }

  fun get(player: Player): List<Skill<*>> {
    return get(player.uuid)
  }

  fun <T> get(playerId: UUID, name: ResourceLocation): Skill<T>? {
    return get(playerId).asSequence()
      .filter { it.name == name }
      .map { it.cast<T>() }
      .firstOrNull()
  }

  fun <T> get(player: Player, name: ResourceLocation): Skill<T>? {
    return get(player.uuid, name)
  }

  fun <T> get(player: Player, name: String): Skill<T>? {
    return get(player.uuid, SkillResourceLocation.of(name))
  }

  @Throws(RegistryItemNotFound::class)
  fun <T> getOrThrow(playerId: UUID, name: ResourceLocation): Skill<T> {
    return get(playerId, name) ?: throw RegistryItemNotFound()
  }

  @Throws(RegistryItemNotFound::class)
  fun <T> getOrThrow(player: Player, name: String): Skill<T> {
    return get(player, name) ?: throw RegistryItemNotFound()
  }

  @Throws(RegistryItemNotFound::class)
  fun <T> getOrDefault(player: Player, name: String): Skill<T> {
    return get(player, name) ?: Skill.findOrThrow(name)
  }

  /**
   * Default response for skill checks. We want to DENY by default any check for a skill.
   */
  const val DEFAULT_SKILL_RESPONE = false

  internal fun <T> can(skill: Skill<T>, expectedValue: T?): Boolean {
    return SkillType.find(skill)?.can(skill, expectedValue) ?: DEFAULT_SKILL_RESPONE
  }

  /**
   * Determines if a Player has a skill using a ResourceLocation, optionally at an expected value
   */
  @JvmOverloads
  fun <T> can(playerId: UUID, skillName: ResourceLocation, expectedValue: T? = null): Boolean {
    return get<T>(playerId, skillName)?.let { can(it, expectedValue) } ?: DEFAULT_SKILL_RESPONE
  }

  @JvmOverloads
  fun <T> can(player: Player, skillName: ResourceLocation, expectedValue: T? = null): Boolean {
    return can(player.uuid, skillName, expectedValue)
  }

  fun <T> can(player: Player, skillName: String, expectedValue: T? = null): Boolean {
    return can(player, SkillResourceLocation.of(skillName), expectedValue)
  }

  internal fun upsert(player: UUID, skill: Skill<*>): List<Skill<*>> {
    return Players.upsert(player, skill)
  }

  fun <T> improve(player: Player, skillName: String, min: T? = null, max: T? = null): Boolean {
    val skill = getOrThrow<T>(player, skillName)
    val nextValue = SkillType.findOrThrow<T>(skill.type).getNextValue(skill, min, max)

    return set(player, skill, nextValue)
  }

  fun <T> degrade(player: Player, skillName: String, min: T? = null, max: T? = null): Boolean {
    val skill = getOrThrow<T>(player, skillName)
    val nextValue = SkillType.findOrThrow<T>(skill.type).getPrevValue(skill, min, max)

    return set(player, skill, nextValue)
  }

  fun <T> set(player: Player, skill: Skill<T>, newValue: T? = null): Boolean {
    val value = newValue ?: skill.value
    val oldSkill = get<T>(player, skill.name) ?: Skill.find(skill.name) ?: return false

    // Don't change if all changes are spent
    if (!oldSkill.areChangesAllowed()) {
      return false
    }

    // Don't change if it's not an allowed value
    if (!skill.isAllowedValue(value)) {
      return false
    }

    // Don't change if it's the same value
    if (oldSkill.value == value) {
      return false
    }

    val newSkill = oldSkill.change(newValue)

    // Don't change if the team doesn't allow it
    if (!Team.allows(player, newSkill)) {
      return false
    }

    // Update the whole team
    if (skill.teamMode.isShared) {
      return Team.updateTeam(player, newSkill)
    }

    // Update just the one player
    val newSkills = upsert(player.uuid, newSkill)
    PlayerSkills.emitSkillChanged(player, newSkill, oldSkill)

    return newSkills.contains(newSkill)
  }

  fun <T> set(player: Player, skill: ResourceLocation, newValue: T? = null): Boolean {
    return get<T>(player, skill)?.let { set(player, it, newValue) } ?: false
  }

  fun <T> set(player: Player, skill: String, newValue: T? = null): Boolean {
    return set(player, SkillResourceLocation.of(skill), newValue)
  }

  /**
   * Reset a Skill to the default value in the Skills registry
   */
  fun <T> reset(player: Player, skill: ResourceLocation): Boolean {
    return Skill.find<T>(skill)?.let { set(player, skill, it.value) } ?: false
  }

  fun <T> reset(player: Player, skill: String): Boolean {
    return reset<T>(player, SkillResourceLocation.of(skill))
  }
}
