package net.impleri.playerskills.skills.tiered

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.resources.ResourceLocation

open class TieredSkill(
  name: ResourceLocation,
  value: String?,
  description: String?,
  options: List<String>?,
  changesAllowed: Int?,
  teamMode: TeamMode?,
  notify: Boolean?,
  notifyKey: String?,
) : Skill<String>(
  name,
  TieredSkillType.NAME,
  value,
  description,
  options ?: ArrayList(),
  changesAllowed ?: Skill.UNLIMITED_CHANGES,
  teamMode ?: TeamMode.off(),
  notify ?: false,
  notifyKey,
) {
  override fun copy(value: String?, changesAllowed: Int): Skill<String> {
    return TieredSkill(name, value, description, options, changesAllowed, teamMode, notify, notifyKey)
  }
}
