package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
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
      PlayerSkillsLogger.SKILLS.error("Unable to parse $value into an integer")
    }
    return null
  }

  private fun getNumericValue(value: Double?, fallback: Double = 0.0): Double {
    return value ?: fallback
  }

  override fun can(skill: Skill<Double>, expectedValue: Double?): Boolean {
    val givenValue = getNumericValue(skill.value)
    val testValue = getNumericValue(expectedValue)

    PlayerSkillsLogger.SKILLS.debug("Checking if player can ${skill.name} (is ${skill.value}->$givenValue >= $testValue<-$expectedValue)")

    return givenValue >= testValue
  }

  override fun getPrevValue(skill: Skill<Double>, min: Double?, max: Double?): Double {
    val currentValue = getNumericValue(skill.value)
    val step = (skill as NumericSkill).step

    val nextVal = currentValue - step
    val maxVal = if (max == null) nextVal else nextVal.coerceAtMost(max)

    return maxVal.coerceAtLeast(getNumericValue(min))
  }

  override fun getNextValue(skill: Skill<Double>, min: Double?, max: Double?): Double {
    val currentValue = getNumericValue(skill.value)
    val step = (skill as NumericSkill).step

    val minVal = (currentValue + step).coerceAtLeast(getNumericValue(min, step))

    return if (max == null) minVal else minVal.coerceAtMost(max)
  }

  companion object {
    var NAME = SkillResourceLocation.of("numeric")
  }
}
