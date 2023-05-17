package net.impleri.playerskills.skills.basic

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

open class BasicSkill(
  name: ResourceLocation,
  value: Boolean?,
  description: String?,
  options: List<Boolean>?,
  changesAllowed: Int?,
  teamMode: TeamMode?,
  notify: Boolean?,
  notifyKey: String?,
) : Skill<Boolean>(
  name,
  BasicSkillType.NAME,
  value,
  description,
  options ?: ArrayList(),
  changesAllowed ?: UNLIMITED_CHANGES,
  teamMode ?: TeamMode.off(),
  notify ?: false,
  notifyKey,
) {
  override fun copy(value: Boolean?, changesAllowed: Int): Skill<Boolean> {
    return BasicSkill(name, value, description, options, changesAllowed, teamMode, notify, notifyKey)
  }

  override fun getDefaultNotification(): Component {
    val messageKey =
      if (value == true) "playerskills.notify.basic_skill_enabled" else "playerskills.notify.basic_skill_disabled"
    val skillName = formatSkillName()
    return Component.translatable(messageKey, skillName)
  }
}
