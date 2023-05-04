package net.impleri.playerskills.skills.specialized

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

open class SpecializedSkill(
  name: ResourceLocation?,
  options: List<String>?,
  value: String?,
  description: String?,
  changesAllowed: Int,
  teamMode: TeamMode?,
  notify: Boolean,
  notifyKey: String?,
) : Skill<String>(
  name!!, SpecializedSkillType.NAME, value, description, options!!, changesAllowed, teamMode!!, notify, notifyKey
) {
  override fun copy(value: String?, changesAllowed: Int): Skill<String> {
    return SpecializedSkill(name, options, value, description, changesAllowed, teamMode, notify, notifyKey)
  }

  override fun getDefaultNotification(): Component {
    return Component.translatable(
      "playerskills.notify.specialized_skill_selected",
      formatSkillName(),
      formatSkillValue()
    )
  }
}
