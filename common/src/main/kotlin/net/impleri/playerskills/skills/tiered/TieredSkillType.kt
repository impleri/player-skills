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
    val givenValue = getIndexValue(skill)
    val testValue = getIndexValue(expectedValue, getOptions(skill))

    PlayerSkills.LOGGER.debug("Checking if player can ${skill.name} (is ${skill.value}->$givenValue >= $testValue<-$expectedValue)")

    return givenValue >= testValue
  }

  override fun getPrevValue(skill: Skill<String>, min: String?, max: String?): String? {
    if (!skill.areChangesAllowed()) {
      return null
    }

    val currentValue = getIndexValue(skill)
    val options = getOptions(skill)
    val maxIndex = getIndexValue(max, options)
    val minIndex = getIndexValue(min, options)

    val currentMinusOne = currentValue - 1

    // Ensure we jump down to the max value
    val nextVal = if (max == null) currentMinusOne else currentMinusOne.coerceAtMost(maxIndex)

    // We're stopping at 0, so no fallback needed
    val minVal = if (min == null) 0 else minIndex.coerceAtLeast(0)

    // Decrement the current value if we're over the min
    return if (nextVal >= minVal) getIndexName(nextVal, skill) else null
  }

  override fun getNextValue(skill: Skill<String>, min: String?, max: String?): String? {
    if (!skill.areChangesAllowed()) {
      return null
    }

    val currentValue = getIndexValue(skill)
    val options = getOptions(skill)
    val maxIndex = getIndexValue(max, options)
    val minIndex = getIndexValue(min, options)

    val currentPlusOne = currentValue + 1

    // Ensure we don't go over available options
    val rawNextVal = currentPlusOne.coerceAtMost(options.size)

    // Ensure we jump up to the min value immediately
    val nextVal = if (min == null) rawNextVal else rawNextVal.coerceAtLeast(minIndex)

    // If no max, use nextVal so that we increment
    val maxVal = if (max == null) rawNextVal else rawNextVal.coerceAtMost(maxIndex)

    // Increment the current value if at or below the max
    return if (nextVal <= maxVal) getIndexName(nextVal, skill) else null
  }

  private fun getOptions(skill: Skill<String>): List<String> {
    return skill.options
  }

  private fun getActualIndexValue(value: String?, options: List<String>): Int {
    return if (value == null) -1 else options.indexOf(value)
  }

  private fun getIndexValue(value: String?, options: List<String>): Int {
    val fallbackIndex = -1

    val realFallback = fallbackIndex.coerceAtLeast(0)

    val indexValue = getActualIndexValue(value, options)
    return if (indexValue == -1) realFallback else indexValue
  }

  private fun getIndexValue(skill: Skill<String>): Int {
    return getIndexValue(skill.value, getOptions(skill))
  }

  private fun getIndexName(index: Int, skill: Skill<String>): String {
    return getOptions(skill)[index]
  }

  companion object {
    var NAME = SkillResourceLocation.of("tiered")
  }
}
