package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.integrations.kubejs.api.PlayerDataJS
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as PlayerApi

/**
 * Skills data that gets attached to Player
 */
class MutablePlayerDataJS(player: Player) : PlayerDataJS(
  player,
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
    skillName: String,
    consumer: ((SkillConditionBuilderJS<T>) -> Unit)?,
  ): SkillConditionBuilderJS<T>? {
    return getSkill<T>(skillName)?.let { skill ->
      consumer?.let {
        val builder = SkillConditionBuilderJS(skill)
        consumer(builder)
        builder
      }
    }
  }

  fun getAll(): List<Skill<*>> {
    return PlayerApi.get(player)
  }

  fun getSkills(): List<Skill<*>> {
    return Skill.all()
  }

  fun <T> set(skillName: String, newValue: T, consumer: ((SkillConditionBuilderJS<T>) -> Unit)?): Boolean {
    getBuilderFor(skillName, consumer)?.let {
      if (!it.shouldChange()) {
        return false
      }
    }

    return PlayerApi.set(player, skillName, newValue)
  }

  fun <T> set(skillName: String, newValue: T): Boolean {
    return set(skillName, newValue, null)
  }

  fun <T> improve(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)?): Boolean {
    return getBuilderFor(skillName, consumer)?.let {
      return if (it.shouldChange()) {
        return PlayerApi.improve(player, skillName, it.min, it.max)
      } else {
        false
      }
    } ?: PlayerApi.improve<T>(player, skillName)
  }

  fun <T> improve(skillName: String): Boolean {
    return improve<T>(skillName, null)
  }

  fun <T> degrade(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)?): Boolean {
    return getBuilderFor(skillName, consumer)?.let {
      return if (it.shouldChange()) {
        PlayerApi.degrade(player, skillName, it.min, it.max)
      } else {
        false
      }
    } ?: PlayerApi.degrade<T>(player, skillName)
  }

  fun <T> degrade(skillName: String): Boolean {
    return degrade<T>(skillName, null)
  }

  fun <T> reset(skillName: String, consumer: ((SkillConditionBuilderJS<T>) -> Unit)?): Boolean {
    getBuilderFor(skillName, consumer)?.let {
      if (!it.shouldChange()) {
        return false
      }
    }

    return PlayerApi.reset<T>(player, skillName)
  }

  fun <T> reset(skillName: String): Boolean {
    return reset<T>(skillName, null)
  }
}
