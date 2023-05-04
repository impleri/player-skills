package net.impleri.playerskills.integrations.ftbquests

import net.impleri.playerskills.api.Skill
import net.minecraft.resources.ResourceLocation

object PlayerSkillsIntegration {
  fun init() {
    SkillTaskTypes.init()
    SkillRewardTypes.init()
  }

  fun getSkills(skillType: ResourceLocation): List<ResourceLocation> {
    return Skill.all()
      .filter { it.type === skillType }
      .map { it.name }
  }
}
