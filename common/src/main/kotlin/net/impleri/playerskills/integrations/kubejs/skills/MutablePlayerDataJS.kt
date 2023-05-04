package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.integrations.kubejs.api.PlayerDataJS
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as PlayerApi

/**
 * Skills data that gets attached to Player
 */
class MutablePlayerDataJS(player: Player) : PlayerDataJS(
  player
) {
  @HideFromJS
  private fun <T> getSkill(skillName: String): Skill<T>? {
    return PlayerApi.get(player, skillName)
  }

  @HideFromJS
  private fun <T> getSkillType(skill: Skill<T>?): SkillType<T>? {
    return skill?.let { SkillType.find(it) }
  }

  @HideFromJS
  private fun <T> getBuilderFor(
    skill: Skill<T>,
    consumer: ((SkillConditionBuilderJS<T>) -> Unit)?,
  ): SkillConditionBuilderJS<T>? {
    return consumer?.let {
      val builder = SkillConditionBuilderJS(skill, player)
      consumer(builder)
      builder
    }
  }

  @HideFromJS
  private fun <T> handleChange(skill: Skill<T>, newValue: T? = null): Boolean {
    if (newValue != null && skill.areChangesAllowed() && skill.isAllowedValue(newValue)) {
      PlayerSkills.LOGGER.debug("Should change ${skill.name} to $newValue")
      return PlayerApi.set(player, skill, newValue)
    }

    return false
  }

  val all: List<Skill<*>>
    get() = PlayerApi.get(player)
  val skills: List<Skill<*>>
    get() = Skill.all()

  @JvmOverloads
  fun <T> set(skillName: String, newValue: T, consumer: ((SkillConditionBuilderJS<T>) -> Unit)? = null): Boolean {
    getSkill<T>(skillName)?.let {
      var shouldChange = it.value !== newValue

      val builder = getBuilderFor(it, consumer)
      if (builder != null) {
        shouldChange = builder.shouldChange() && shouldChange
      }

      if (shouldChange) {
        PlayerSkills.LOGGER.debug("Should set ${it.name} to $newValue.")
        return handleChange(it, newValue)
      }
    }

    return false
  }

  @JvmOverloads
  fun <T> improve(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)? = null): Boolean {
    return getSkill<T>(skillName)?.let {

      val type = getSkillType(it) ?: return false
      val builder = getBuilderFor(it, consumer)

      var newValue = type.getNextValue(it)
      if (builder != null) {
        newValue = builder.calculateNext()
      }
      handleChange(it, newValue)
    } ?: false
  }

  @JvmOverloads
  fun <T> degrade(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)? = null): Boolean {
    return getSkill<T>(skillName)?.let {
      val type = getSkillType(it) ?: return false
      val builder = getBuilderFor(it, consumer)

      var newValue = type.getPrevValue(it)
      if (builder != null) {
        newValue = builder.calculatePrev()
      }

      handleChange(it, newValue)
    } ?: false
  }

  @JvmOverloads
  fun <T> reset(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)? = null): Boolean {
    return getSkill<T>(skillName)?.let {
      val builder = getBuilderFor(it, consumer)

      val defaultSkill: Skill<T>? = Skill.find(skillName)

      var shouldChange = it.value !== defaultSkill?.value
      if (builder != null) {
        shouldChange = builder.shouldChange() && shouldChange
      }

      if (!shouldChange) {
        return false
      }

      PlayerSkills.LOGGER.debug("Should reset ${it.name}.")
      PlayerApi.reset<T>(player, it.name)
    } ?: false
  }
}
