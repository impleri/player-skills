package net.impleri.playerskills.integrations.kubejs

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType

internal class PlayerSkillsKubeJSWrapper {
  val skills: List<Skill<*>>
    get() = Skill.all()
  val skillTypes: List<SkillType<*>>
    get() = SkillType.all()
  val types: List<SkillType<*>>
    get() = skillTypes
}
