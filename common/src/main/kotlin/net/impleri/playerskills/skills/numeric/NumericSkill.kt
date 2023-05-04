package net.impleri.playerskills.skills.numeric

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.resources.ResourceLocation

open class NumericSkill(
  name: ResourceLocation?,
  value: Double?,
  description: String?,
  options: List<Double>?,
  changesAllowed: Int,
  teamMode: TeamMode?,
  notify: Boolean,
  notifyKey: String?,
) : Skill<Double>(
  name!!, NumericSkillType.NAME, value, description, options!!, changesAllowed, teamMode!!, notify, notifyKey
) {
  override fun copy(value: Double?, changesAllowed: Int): Skill<Double> {
    return NumericSkill(name, value, description, options, changesAllowed, teamMode, notify, notifyKey)
  }
}
