package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.minecraft.resources.ResourceLocation

class SpecializedSkillTask(quest: Quest?) : TieredSkillTask(quest) {
  override val skillType: ResourceLocation
    get() = SpecializedSkillType.NAME

  override fun getType(): TaskType {
    return SkillTaskTypes.SPECIALIZED_SKILL
  }
}
