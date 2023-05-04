package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.task.TaskTypes
import net.impleri.playerskills.utils.SkillResourceLocation.of
import net.minecraft.network.chat.Component

interface SkillTaskTypes {
  companion object {
    fun init() {
      BASIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.basic_skill"))
      NUMERIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"))
      TIERED_SKILL.setDisplayName(Component.translatable("playerskills.quests.tiered_skill"))
      SPECIALIZED_SKILL.setDisplayName(Component.translatable("playerskills.quests.specialized_skill"))
    }

    val BASIC_SKILL = TaskTypes.register(
      of("basic_skill_task"),
      { BasicSkillTask(it) },
      { Icon.getIcon("minecraft:item/wooden_hoe") })

    val NUMERIC_SKILL = TaskTypes.register(
      of("numeric_skill_task"),
      { NumericSkillTask(it) },
      { Icon.getIcon("minecraft:item/iron_hoe") })

    val TIERED_SKILL = TaskTypes.register(
      of("tiered_skill_task"),
      { TieredSkillTask(it) },
      { Icon.getIcon("minecraft:item/golden_hoe") })

    val SPECIALIZED_SKILL = TaskTypes.register(
      of("specialized_skill_task"),
      { SpecializedSkillTask(it) },
      { Icon.getIcon("minecraft:item/diamond_hoe") })
  }
}
