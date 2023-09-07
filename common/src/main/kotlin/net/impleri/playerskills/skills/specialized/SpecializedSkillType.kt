package net.impleri.playerskills.skills.specialized

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

class SpecializedSkillType : SkillType<String>() {
  override val name: ResourceLocation
    get() = NAME

  private fun getOptions(skill: Skill<String>): List<String> {
    return skill.options
  }

  override fun castToString(value: String?): String? {
    return value
  }

  public override fun castFromString(value: String?): String? {
    return value
  }

  override fun can(skill: Skill<String>, expectedValue: String?): Boolean {
    if (expectedValue == null) {
      return skill.value != null
    }
    PlayerSkillsLogger.SKILLS.debug("Checking if player can ${skill.name} (is ${skill.value} == $expectedValue)")
    return expectedValue == skill.value
  }

  override fun getPrevValue(skill: Skill<String>, min: String?, max: String?): String {
    return ""
  }

  override fun getNextValue(skill: Skill<String>, min: String?, max: String?): String {
    return ""
  }

  companion object {
    var NAME = SkillResourceLocation.of("specialized")
  }
}
