package net.impleri.playerskills.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.skills.registry.Players
import net.impleri.playerskills.skills.registry.Players.close
import net.impleri.playerskills.skills.registry.Players.open
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.pow
import net.impleri.playerskills.api.Player as PlayerApi

// @TODO: Reorganize this
abstract class Team {
  /**
   * Mod-specific implementation to get all other players "on the same team" as the given player ID
   */
  protected abstract fun getTeamMembersFor(playerId: UUID): List<UUID>

  protected fun getTeamMembersFor(player: Player): List<UUID> {
    return getTeamMembersFor(player.uuid)
  }

  private class StubTeam : Team() {
    override fun getTeamMembersFor(playerId: UUID): List<UUID> {
      return listOf(playerId)
    }
  }

  companion object {
    private var INSTANCE: Team = StubTeam()

    /**
     * Sets the TeamApi instance to be used by the static methods
     */
    fun setInstance(instance: Team) {
      INSTANCE = instance
    }

    /**
     * Does the team have room for the skill
     */
    fun <T> allows(player: Player, skill: Skill<T>): Boolean {
      val players = INSTANCE.getTeamMembersFor(player)
      val teamMode = skill.teamMode
      if (players.size < 2 || teamMode.isOff || teamMode.isShared) {
        PlayerSkills.LOGGER.info("No need to bother team")
        return true
      }
      val count = countWith(players, skill).toDouble()

      val limit: Double = when {
        (teamMode.isLimited) -> teamMode.rate!!
        (teamMode.isProportional) -> {
          val percentage = teamMode.rate!! / 100
          val teamSize = players.size
          ceil(teamSize * percentage)
        }

        (teamMode.isSplitEvenly) -> {
          val options = skill.options.size
          val teamSize = players.size.toDouble()
          ceil(teamSize / options)
        }

        (teamMode.isPyramid && skill.options.indexOf(skill.value) > -1) -> {
          val newIndex = skill.options.indexOf(skill.value)

          // Always allow the lowest tier
          skill.options
            .reversed() // reverse the tiers so that the highest/smallest is first
            .map { skill.options.indexOf(it) } // convert tier to its current index
            .map { 2.0.pow(it.toDouble()) } // scale each tier as 2^n where n = index
            .reversed()[newIndex]
        }

        else -> count + 1
      }

      val allowed = count < limit

      PlayerSkills.LOGGER.info("Does the team allow updating skill? $allowed ($count < $limit)")

      return allowed
    }

    /**
     * Set the skill for the entire team
     */
    fun <T> updateTeam(player: Player, newSkill: Skill<T>): Boolean {
      // Team Shared: Update for all team members
      val teamMembers = INSTANCE.getTeamMembersFor(player)
      PlayerSkills.LOGGER.debug("Syncing skills with team: ${teamMembers.map { it.toString() }.joinToString { ", " }}")

      // Emit SkillChanged event to all team members currently logged in
      syncTeam(teamMembers, newSkill, player.server)
      return true
    }

    /**
     * Syncs the player's shared skills with the rest of the team, overriding any progress one may have
     */
    fun syncFromPlayer(player: ServerPlayer): Boolean {
      PlayerSkills.LOGGER.debug("Syncing skills from ${player.name}")

      getSharedSkills(player).forEach { updateTeam(player, it) }

      return true
    }

    /**
     * Syncs the "best" skill value for each shared skill currently help by the team with the rest of the team
     */
    fun syncEntireTeam(player: ServerPlayer): Boolean {
      PlayerSkills.LOGGER.debug("Syncing entire team connected to ${player.name}")

      val teamMembers = INSTANCE.getTeamMembersFor(player)
      val offlineMembers = open(teamMembers)
      val onlineMembers = teamMembers.filter { !offlineMembers.contains(it) }

      val maxSkills = getSharedSkills(player).map { getMaxSkill(teamMembers, it) }

      maxSkills.forEach { syncTeam(onlineMembers, it, player.getServer()) }
      maxSkills.forEach { syncTeam(offlineMembers, it, null) }

      close(offlineMembers)

      return true
    }

    private fun <T> getMaxSkill(teamMembers: List<UUID>, skill: Skill<T>): Skill<T> {
      return teamMembers
        .mapNotNull { PlayerApi.get<T>(it, skill.name) }
        .maxWithOrNull { a: Skill<T>, b: Skill<T> ->
          val aGreaterOrEqual = PlayerApi.can(b, a.value)
          val bGreaterOrEqual = PlayerApi.can(a, b.value)

          when {
            (aGreaterOrEqual && bGreaterOrEqual) -> 0
            (aGreaterOrEqual) -> -1
            else -> 1
          }
        } ?: skill
    }

    private fun <T> syncTeam(teamMembers: List<UUID>, skill: Skill<T>, server: MinecraftServer?) {
      val oldSkillsMap = upsertTeam(teamMembers, skill)

      // Emit SkillChanged event to all team members currently logged in
      if (server != null) {
        val playerList = server.playerList
        oldSkillsMap
          .mapKeys { playerList.getPlayer(it.key) }
          .filterKeys { it != null }
          .forEach {
            PlayerSkills.emitSkillChanged(it.key!!, skill, it.value)
          }
      }
    }

    private fun getSharedSkills(player: Player): List<Skill<*>> {
      return PlayerApi.get(player)
        .filter { it.teamMode.isShared }
    }

    private fun <T> upsertTeam(playerIds: List<UUID>, skill: Skill<T>): Map<UUID, Skill<T>> {
      return playerIds.mapNotNull {
        ensurePlayerOpen(it) {
          val oldSkill = PlayerApi.get(it, skill.name) ?: skill

          // Only update if the new skill is better
          if (!PlayerApi.can(oldSkill, skill.value)) {
            PlayerApi.upsert(it, skill)
            return@ensurePlayerOpen it to oldSkill
          }

          return@ensurePlayerOpen null
        }
      }.toMap()
    }

    private fun <T> countWith(playerIds: List<UUID>, skill: Skill<T>): Int {
      return playerIds.count { ensurePlayerOpen(it) { PlayerApi.can(it, skill.name, skill.value) } }
    }

    private fun <V> ensurePlayerOpen(playerId: UUID, callback: () -> V): V {
      val isActive = Players.has(playerId)
      if (!isActive) {
        Players.open(playerId)
      }

      val value = callback()

      if (!isActive) {
        Players.close(playerId)
      }

      return value
    }
  }
}
