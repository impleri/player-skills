package net.impleri.playerskills.skills.registry

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.skills.registry.storage.SkillStorage.read
import net.impleri.playerskills.skills.registry.storage.SkillStorage.write
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import java.util.UUID

object Players {
  /**
   * MUTABLE in-memory cache
   */
  private val players: MutableMap<UUID, List<Skill<*>>> = HashMap()

  /**
   * Helper function to save to in-memory cache only
   */
  private fun saveToCache(playerId: UUID, skills: List<Skill<*>>) {
    players[playerId] = skills
  }

  /**
   * Helper function to save to in-memory cache AND persistent storage
   */
  private fun save(playerId: UUID, skills: List<Skill<*>>) {
    saveToCache(playerId, skills)
    writeToStorage(playerId, skills)
  }

  /**
   * Get a copy of the in memory cache
   */
  fun entries(): Map<UUID, List<Skill<*>>> {
    return players.toMap()
  }

  fun has(playerId: UUID): Boolean {
    return players.containsKey(playerId)
  }

  /**
   * Get all skills for a player from the in-memory cache
   */
  fun get(playerId: UUID): List<Skill<*>> {
    return players[playerId] ?: return readFromStorage(playerId)
  }

  private fun handleOpenFor(playerId: UUID): List<Skill<*>> {
    PlayerSkillsLogger.SKILLS.info("Opening player $playerId, ensuring saved skills are still valid")

    // Get all the names of the registered skills
    val registeredSkillNames = Skills.entries()
      .map { it.name }

    val rawSkills = this.get(playerId)

    // Get an intersection of saved skills that are still registered
    val savedSkills = rawSkills
      .filter { registeredSkillNames.contains(it.name) }

    val savedSkillNames = savedSkills
      .map { it.name }

    val newSkills = Skills.entries()
      .filter { !savedSkillNames.contains(it.name) }

    return savedSkills + newSkills
  }

  /**
   * Instantiates the in-memory cache for a player. Note that this will automatically prune saved skills that do not
   * match the skill type in the Skills Registry as well as those which no longer exist at all in the Skills Registry
   */
  fun open(playerId: UUID) {
    val skills = handleOpenFor(playerId)

    // Immediately sync in-memory cache AND persistent storage with updated skills set
    save(playerId, skills)
  }

  fun open(playerIds: List<UUID>): List<UUID> {
    val playersAdded = playerIds.filter { !players.containsKey(it) }

    val skillsList = playerIds.associateWith { handleOpenFor(it) }

    players.putAll(skillsList)

    return playersAdded
  }

  /**
   * Helper function to upsert a skill for a player in memory only
   */
  private fun handleUpsert(playerId: UUID, skill: Skill<*>): List<Skill<*>> {
    // Only replace skill with same name AND type (edge case of same name but different type is handled in openPlayer)
    val filteredSkills = this.get(playerId).filter { skill.name != it.name }

    val addedSkills = listOf(skill)

    val newSkills = filteredSkills + addedSkills

    saveToCache(playerId, newSkills)

    return newSkills
  }

  /**
   * Upsert a skill for a player and saves to persistent storage
   */
  fun upsert(playerId: UUID, skill: Skill<*>): List<Skill<*>> {
    val newSkills = handleUpsert(playerId, skill)

    save(playerId, newSkills)

    return newSkills
  }

  /**
   * Add a skill to a player only if the player does not have it
   */
  fun add(playerId: UUID, skill: Skill<*>): List<Skill<*>> {
    val existingSkills = this.get(playerId)

    // Skill already exists, so do nothing
    if (existingSkills.any { skill == it }) {
      return existingSkills
    }
    val newSkills = handleUpsert(playerId, skill)
    save(playerId, newSkills)
    return newSkills
  }

  /**
   * Completely remove a skill from a player
   */
  fun remove(playerId: UUID, name: ResourceLocation): List<Skill<*>> {
    val newSkills = this.get(playerId).filter { it.name !== name }

    save(playerId, newSkills)

    return newSkills
  }

  private fun handleCloseFor(playerId: UUID) {
    PlayerSkillsLogger.SKILLS.info("Closing player $playerId, ensuring skills are saved")

    val skills = this.get(playerId)

    writeToStorage(playerId, skills)
  }

  /**
   * Save a player's skills to persistent storage then remove player from in-memory cache
   */
  fun close(playerId: UUID) {
    handleCloseFor(playerId)

    players.remove(playerId)
  }

  fun close(playerIds: List<UUID>) {
    playerIds.forEach { handleCloseFor(it) }
    playerIds.forEach { players.remove(it) }
  }

  /**
   * Save multiple players' skills to persistent storage then remove them from in-memory cache
   */
  fun close(): List<UUID> {
    val playerIds = players.keys.toList()
    close(playerIds)

    // Wipe away any players that are somehow still present
    players.clear()
    return playerIds
  }

  private fun readFromStorage(playerId: UUID): List<Skill<*>> {
    PlayerSkillsLogger.SKILLS.debug("Restoring saved skills for $playerId")
    return read(playerId).mapNotNull { SkillType.unserializeFromString(it) }
  }

  private fun writeToStorage(playerId: UUID, skills: List<Skill<*>>) {
    PlayerSkillsLogger.SKILLS.debug("Saving skills for $playerId")

    val rawSkills: List<String> = skills
      .map { SkillType.serializeToString(it) }
      .filter { it.isNotEmpty() }

    write(playerId, rawSkills)
  }
}
