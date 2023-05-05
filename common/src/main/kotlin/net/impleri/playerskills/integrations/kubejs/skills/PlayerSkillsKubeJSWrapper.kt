package net.impleri.playerskills.integrations.kubejs.skills

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType

internal class PlayerSkillsKubeJSWrapper {
  fun getSkills(): List<Skill<*>> {
    return Skill.all()
  }

  fun getSkillTypes(): List<SkillType<*>> {
    return SkillType.all()
  }

  fun getTypes(): List<SkillType<*>> {
    return getSkillTypes()
  }
}
