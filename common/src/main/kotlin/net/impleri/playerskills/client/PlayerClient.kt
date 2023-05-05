package net.impleri.playerskills.client

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

object PlayerClient {
  /**
   * Determines if a Player has a skill, optionally at an expected value
   */
  private fun <T> can(skill: Skill<T>, expectedValue: T?): Boolean {
    return SkillType.find(skill)?.can(skill, expectedValue) ?: false
  }

  @JvmOverloads
  fun <T> can(skillName: ResourceLocation, expectedValue: T? = null): Boolean {
    return Registry.get().asSequence()
      .filter { it.name == skillName }
      .firstOrNull()
      ?.let { can(it.cast(), expectedValue) } ?: false
  }

  @JvmOverloads
  fun <T> can(skillName: String, expectedValue: T? = null): Boolean {
    return can<T?>(
      SkillResourceLocation.of(skillName),
      expectedValue,
    )
  }

  @JvmOverloads
  @Throws(MismatchedClientPlayerException::class)
  fun <T> can(player: Player, skill: Skill<T>, expectedValue: T? = null): Boolean {
    checkPlayer(player)
    return can(skill, expectedValue)
  }

  @JvmOverloads
  @Throws(MismatchedClientPlayerException::class)
  fun <T> can(player: Player, skillName: ResourceLocation, expectedValue: T? = null): Boolean {
    checkPlayer(player)
    return can<T?>(skillName, expectedValue)
  }

  @JvmOverloads
  @Throws(MismatchedClientPlayerException::class)
  fun <T> can(player: Player, skillName: String, expectedValue: T? = null): Boolean {
    checkPlayer(player)
    return can<T?>(skillName, expectedValue)
  }

  @Throws(MismatchedClientPlayerException::class)
  private fun checkPlayer(player: Player) {
    if (!player.getLevel().isClientSide) {
      throw MismatchedClientPlayerException()
    }

    val localPlayer = Minecraft.getInstance().player
    if (localPlayer == null || localPlayer != player) {
      throw MismatchedClientPlayerException()
    }
  }
}
