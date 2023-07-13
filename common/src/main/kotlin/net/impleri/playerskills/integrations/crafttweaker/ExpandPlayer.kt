package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.api.Skill
import net.minecraft.world.entity.player.Player
import org.openzen.zencode.java.ZenCodeType
import kotlin.math.PI
import net.impleri.playerskills.api.Player as PlayerApi

@ZenRegister
@ZenCodeType.Expansion("crafttweaker.api.entity.type.player.Player")
object ExpandPlayer {
  @ZenCodeType.Getter("skills")
  @JvmStatic
  fun getSkills(player: Player): List<Skill<*>> {
    return PlayerApi.get(player)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun can(player: Player, skillName: String, expectedValue: Boolean): Boolean {
    return PlayerApi.can(
      player,
      skillName,
      expectedValue,
    )
  }

  @ZenCodeType.Method
  @JvmStatic
  fun can(player: Player, skillName: String, expectedValue: Double): Boolean {
    return PlayerApi.can(
      player,
      skillName,
      expectedValue,
    )
  }

  @ZenCodeType.Method
  @JvmStatic
  fun can(player: Player, skillName: String, expectedValue: String): Boolean {
    return PlayerApi.can(
      player,
      skillName,
      expectedValue,
    )
  }

  @ZenCodeType.Method
  @JvmStatic
  fun can(player: Player, skillName: String): Boolean {
    return PlayerApi.can<Any>(
      player,
      skillName,
    )
  }

  @ZenCodeType.Method
  @JvmStatic
  fun cannot(player: Player, skill: String, expectedValue: Boolean): Boolean {
    return !can(player, skill, expectedValue)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun cannot(player: Player, skill: String, expectedValue: Double): Boolean {
    return !can(player, skill, expectedValue)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun cannot(player: Player, skill: String, expectedValue: String): Boolean {
    return !can(player, skill, expectedValue)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun cannot(player: Player, skill: String): Boolean {
    return !can(player, skill)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun resetSkill(player: Player, skillName: String): Boolean {
    return PlayerApi.reset<Any>(player, skillName)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun setSkill(player: Player, skillName: String, newValue: Boolean): Boolean {
    return PlayerApi.set(player, skillName, newValue)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun improveSkill(
    player: Player,
    skillName: String,
  ): Boolean {
    return PlayerApi.improve<Boolean>(player, skillName)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun degradeSkill(
    player: Player,
    skillName: String,
  ): Boolean {
    return PlayerApi.degrade<Boolean>(player, skillName)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun setSkill(player: Player, skillName: String, newValue: Double): Boolean {
    return PlayerApi.set(player, skillName, newValue)
  }

  private const val NULLISH_DOUBLE = -PI

  private fun recastDoubleToNull(value: Double): Double? {
    return if (value == NULLISH_DOUBLE) null else value
  }

  @ZenCodeType.Method
  @JvmStatic
  fun improveSkill(
    player: Player,
    skillName: String,
    @ZenCodeType.OptionalDouble(NULLISH_DOUBLE) min: Double,
    @ZenCodeType.OptionalDouble(NULLISH_DOUBLE) max: Double,
  ): Boolean {
    return PlayerApi.improve(player, skillName, recastDoubleToNull(min), recastDoubleToNull(max))
  }

  @ZenCodeType.Method
  @JvmStatic
  fun degradeSkill(
    player: Player,
    skillName: String,
    @ZenCodeType.OptionalDouble(NULLISH_DOUBLE) min: Double,
    @ZenCodeType.OptionalDouble(NULLISH_DOUBLE) max: Double,
  ): Boolean {
    return PlayerApi.degrade(player, skillName, recastDoubleToNull(min), recastDoubleToNull(max))
  }

  private fun recastStringToNull(value: String): String? {
    return value.ifEmpty { null }
  }

  @ZenCodeType.Method
  @JvmStatic
  fun setSkill(player: Player, skillName: String, newValue: String): Boolean {
    return PlayerApi.set(player, skillName, newValue)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun improveSkill(
    player: Player,
    skillName: String,
    @ZenCodeType.OptionalString min: String,
    @ZenCodeType.OptionalString max: String,
  ): Boolean {
    return PlayerApi.improve(player, skillName, recastStringToNull(min), recastStringToNull(max))
  }

  @ZenCodeType.Method
  @JvmStatic
  fun degradeSkill(
    player: Player,
    skillName: String,
    @ZenCodeType.OptionalString min: String,
    @ZenCodeType.OptionalString max: String,
  ): Boolean {
    return PlayerApi.degrade(player, skillName, recastStringToNull(min), recastStringToNull(max))
  }
}
