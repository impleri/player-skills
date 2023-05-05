package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

class NumericSkillType : SkillType<Double>() {
  override val name: ResourceLocation
    get() = NAME

  override fun castToString(value: Double?): String {
    return value?.toString() ?: ""
  }

  public override fun castFromString(value: String?): Double? {
    try {
      return if (value.isNullOrBlank()) null else value.toDouble()
    } catch (e: NumberFormatException) {
      PlayerSkills.LOGGER.error("Unable to parse $value into an integer")
    }
    return null
  }

  private fun getNumericValue(value: Double?, fallback: Double = 0.0): Double {
    return value ?: fallback
  }

  private fun getNumericValue(skill: Skill<Double>): Double {
    return getNumericValue(skill.value)
  }

  override fun can(skill: Skill<Double>, expectedValue: Double?): Boolean {
    val givenValue = getNumericValue(skill)
    val testValue = getNumericValue(expectedValue)

    PlayerSkills.LOGGER.debug("Checking if player can ${skill.name} (is ${skill.value}->$givenValue >= $testValue<-$expectedValue)")

    return givenValue >= testValue
  }

  override fun getPrevValue(skill: Skill<Double>, min: Double?, max: Double?): Double? {
    if (!skill.areChangesAllowed()) {
      return null
    }

    val currentValue = getNumericValue(skill)

    // Ensure we jump down to the max value
    val nextVal = if (max == null) currentValue else (currentValue - 1).coerceAtMost(max)

    // We're stopping at 0, so no fallback needed
    val minVal = getNumericValue(min)

    // Decrement the current value if we're over the min
    return if (nextVal >= minVal) nextVal else null
  }

  override fun getNextValue(skill: Skill<Double>, min: Double?, max: Double?): Double? {
    if (!skill.areChangesAllowed()) {
      return null
    }
    val currentValue = getNumericValue(skill)

    // Ensure we jump up to the min value immediately
    val nextVal = if (min == null) currentValue else (currentValue + 1).coerceAtLeast(min)

    // If no max, use nextVal so that we increment
    val maxVal = getNumericValue(max, nextVal)

    // Increment the current value if below the max
    return if (nextVal <= maxVal) nextVal else null
  }

  companion object {
    var NAME = SkillResourceLocation.of("numeric")
  }
}
