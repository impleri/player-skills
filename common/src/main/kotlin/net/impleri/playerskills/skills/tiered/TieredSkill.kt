package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.resources.ResourceLocation

open class TieredSkill(
  name: ResourceLocation,
  options: List<String>?,
  value: String?,
  description: String?,
  changesAllowed: Int,
  teamMode: TeamMode?,
  notify: Boolean,
  notifyKey: String?,
) : Skill<String>(
  name,
  TieredSkillType.NAME,
  value,
  description,
  options!!,
  changesAllowed,
  teamMode!!,
  notify,
  notifyKey,
) {
  override fun copy(value: String?, changesAllowed: Int): Skill<String> {
    return TieredSkill(name, options, value, description, changesAllowed, teamMode, notify, notifyKey)
  }
}
