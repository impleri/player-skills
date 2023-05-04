package net.impleri.playerskills.integrations.kubejs.skills

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.minecraft.resources.ResourceLocation

class NumericSkillJS(builder: Builder) : NumericSkill(
  builder.id,
  builder.initialValue,
  builder.description,
  builder.options,
  builder.changesAllowed,
  builder.teamMode,
  builder.notify,
  builder.notifyKey
) {
  class Builder(name: ResourceLocation) : GenericSkillBuilderJS<Double>(name) {
    override fun createObject(): Skill<Double> {
      return NumericSkillJS(this)
    }
  }
}
