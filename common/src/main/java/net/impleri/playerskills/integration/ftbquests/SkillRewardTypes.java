package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.network.chat.Component;

public interface SkillRewardTypes {
    RewardType BASIC_SKILL = RewardTypes.register(SkillResourceLocation.of("basic_skill_reward"), BasicSkillReward::new, () -> Icon.getIcon("minecraft:item/wooden_hoe"));
    RewardType NUMERIC_SKILL = RewardTypes.register(SkillResourceLocation.of("numeric_skill_reward"), NumericSkillReward::new, () -> Icon.getIcon("minecraft:item/iron_hoe"));
    RewardType TIERED_SKILL = RewardTypes.register(SkillResourceLocation.of("tiered_skill_reward"), TieredSkillReward::new, () -> Icon.getIcon("minecraft:item/golden_hoe"));
    RewardType SPECIALIZED_SKILL = RewardTypes.register(SkillResourceLocation.of("specialized_skill_reward"), SpecializedSkillReward::new, () -> Icon.getIcon("minecraft:item/diamond_hoe"));

    static void init() {
        BASIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.basic_skill"));
        NUMERIC_SKILL.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"));
        TIERED_SKILL.setDisplayName(Component.translatable("playerskills.quests.tiered_skill"));
        SPECIALIZED_SKILL.setDisplayName(Component.translatable("playerskills.quests.specialized_skill"));
    }
}
