package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import net.impleri.playerskills.utils.SkillResourceLocation.of
import net.minecraft.network.chat.Component

interface SkillRewardTypes {
  companion object {
    fun init() {
      BASIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.basic_skill"))
      NUMERIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"))
      TIERED_SKILL.setDisplayName(Component.translatable("playerskills.quests.tiered_skill"))
      SPECIALIZED_SKILL.setDisplayName(Component.translatable("playerskills.quests.specialized_skill"))
    }

    val BASIC_SKILL: RewardType = RewardTypes.register(
      of("basic_skill_reward"),
      { BasicSkillReward(it) },
      { Icon.getIcon("minecraft:item/wooden_hoe") },
    )

    val NUMERIC_SKILL: RewardType = RewardTypes.register(
      of("numeric_skill_reward"),
      { NumericSkillReward(it) },
      { Icon.getIcon("minecraft:item/iron_hoe") },
    )

    val TIERED_SKILL: RewardType = RewardTypes.register(
      of("tiered_skill_reward"),
      { TieredSkillReward(it) },
      { Icon.getIcon("minecraft:item/golden_hoe") },
    )

    val SPECIALIZED_SKILL: RewardType = RewardTypes.register(
      of("specialized_skill_reward"),
      { SpecializedSkillReward(it) },
      { Icon.getIcon("minecraft:item/diamond_hoe") },
    )
  }
}
