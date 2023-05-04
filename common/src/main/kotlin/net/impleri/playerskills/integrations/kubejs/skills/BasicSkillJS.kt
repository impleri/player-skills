package net.impleri.playerskills.integrations.kubejs.skills

import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.skills.basic.BasicSkill
import net.minecraft.resources.ResourceLocation

class BasicSkillJS(builder: Builder) : BasicSkill(
  builder.id,
  builder.initialValue,
  builder.description,
  builder.options,
  builder.changesAllowed,
  builder.teamMode,
  builder.notify,
  builder.notifyKey
) {
  class Builder(name: ResourceLocation) : GenericSkillBuilderJS<Boolean>(name) {
    override fun createObject(): Skill<Boolean> {
      return BasicSkillJS(this)
    }
  }
}
