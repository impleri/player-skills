package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

class TieredSkillType : SkillType<String>() {
  override val name: ResourceLocation
    get() = NAME

  override fun castToString(value: String?): String? {
    return value
  }

  public override fun castFromString(value: String?): String? {
    return value
  }

  override fun can(skill: Skill<String>, expectedValue: String?): Boolean {
    val givenValue = getIndexFor(skill.value, skill.options) ?: 0
    val testValue = getIndexFor(expectedValue, skill.options) ?: 1

    PlayerSkills.LOGGER.debug("Checking if player can ${skill.name} (is ${skill.value}->$givenValue >= $testValue<-$expectedValue)")

    return givenValue >= testValue
  }

  override fun getPrevValue(skill: Skill<String>, min: String?, max: String?): String {
    val currentValue = getIndexFor(skill.value, skill.options) ?: -1
    val maxIndex = getIndexFor(max, skill.options) ?: skill.options.size
    val minIndex = getIndexFor(min, skill.options) ?: 0

    val prevVal = (currentValue - 1).coerceAtLeast(minIndex).coerceAtMost(maxIndex)

    return skill.options[prevVal]
  }

  override fun getNextValue(skill: Skill<String>, min: String?, max: String?): String {
    val currentValue = getIndexFor(skill.value, skill.options) ?: -1
    val maxIndex = getIndexFor(max, skill.options) ?: skill.options.size
    val minIndex = getIndexFor(min, skill.options) ?: 0

    val nextVal = (currentValue + 1).coerceAtLeast(minIndex).coerceAtMost(maxIndex)

    return skill.options[nextVal]
  }

  private fun getIndexFor(value: String?, options: List<String>): Int? {
    value ?: return null
    val index = options.indexOf(value)

    return if (index == -1) null else index
  }

  companion object {
    var NAME = SkillResourceLocation.of("tiered")
  }
}
