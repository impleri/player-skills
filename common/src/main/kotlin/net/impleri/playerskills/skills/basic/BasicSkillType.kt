package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.BooleanUtils

class BasicSkillType : SkillType<Boolean>() {
  override val name: ResourceLocation
    get() = NAME

  override fun castToString(value: Boolean?): String {
    return if (value == null) "" else if (value) STRING_VAL_TRUE else STRING_VAL_FALSE
  }

  override fun castFromString(value: String?): Boolean? {
    return if (value.isNullOrBlank()) null else value == STRING_VAL_TRUE
  }

  override fun can(skill: Skill<Boolean>, expectedValue: Boolean?): Boolean {
    val givenValue = BooleanUtils.toBoolean(skill.value)
    val testValue = expectedValue == null || BooleanUtils.toBoolean(expectedValue)
    PlayerSkills.LOGGER.debug(
      "Checking if player can ${skill.name} (does $expectedValue->$testValue == $givenValue<-${skill.value})",
    )
    return testValue == givenValue
  }

  override fun getPrevValue(skill: Skill<Boolean>, min: Boolean?, max: Boolean?): Boolean? {
    if (!skill.areChangesAllowed()) {
      return null
    }
    return if (BooleanUtils.toBoolean(skill.value)) false else null
  }

  override fun getNextValue(skill: Skill<Boolean>, min: Boolean?, max: Boolean?): Boolean? {
    if (!skill.areChangesAllowed()) {
      return null
    }
    return if (BooleanUtils.toBoolean(skill.value)) null else true
  }

  companion object {
    val NAME = SkillResourceLocation.of("basic")

    private const val STRING_VAL_TRUE = "true"
    private const val STRING_VAL_FALSE = "false"
  }
}
