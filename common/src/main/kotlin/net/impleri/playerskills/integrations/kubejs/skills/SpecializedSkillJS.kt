package net.impleri.playerskills.integrations.kubejs.skills

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.minecraft.resources.ResourceLocation

class SpecializedSkillJS(builder: Builder) : SpecializedSkill(
  builder.id,
  builder.initialValue,
  builder.description,
  builder.options,
  builder.changesAllowed,
  builder.teamMode,
  builder.notify,
  builder.notifyKey,
) {
  class Builder(name: ResourceLocation) : GenericSkillBuilderJS<String>(name) {
    override fun createObject(): Skill<String> {
      return SpecializedSkillJS(this)
    }

    fun splitEvenlyAcrossTeam(): Builder {
      teamMode = TeamMode.splitEvenly()
      return this
    }
  }
}
