package net.impleri.playerskills.integrations.kubejs.skills

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.minecraft.resources.ResourceLocation

class TieredSkillJS(builder: Builder) : TieredSkill(
  builder.id,
  builder.options,
  builder.initialValue,
  builder.description,
  builder.changesAllowed,
  builder.teamMode,
  builder.notify,
  builder.notifyKey,
) {
  class Builder(name: ResourceLocation) : GenericSkillBuilderJS<String>(name) {
    override fun createObject(): Skill<String> {
      return TieredSkillJS(this)
    }

    fun pyramid(): Builder {
      teamMode = TeamMode.pyramid()
      return this
    }
  }
}
