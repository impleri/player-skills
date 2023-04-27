package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;

public interface SkillTaskTypes {
    TaskType BASIC_SKILL = TaskTypes.register(SkillResourceLocation.of("basic_skill_task"), BasicSkillTask::new, () -> Icon.getIcon("minecraft:item/wooden_hoe"));
    TaskType NUMERIC_SKILL = TaskTypes.register(SkillResourceLocation.of("numeric_skill_task"), NumericSkillTask::new, () -> Icon.getIcon("minecraft:item/iron_hoe"));
    TaskType TIERED_SKILL = TaskTypes.register(SkillResourceLocation.of("tiered_skill_task"), TieredSkillTask::new, () -> Icon.getIcon("minecraft:item/golden_hoe"));
    TaskType SPECIALIZED_SKILL = TaskTypes.register(SkillResourceLocation.of("specialized_skill_task"), SpecializedSkillTask::new, () -> Icon.getIcon("minecraft:item/diamond_hoe"));

    static void init() {
        BASIC_SKILL.setDisplayName(new TranslatableComponent("playerskills.quests.basic_skill"));
        NUMERIC_SKILL.setDisplayName(new TranslatableComponent("playerskills.quests.numeric_skill"));
        TIERED_SKILL.setDisplayName(new TranslatableComponent("playerskills.quests.tiered_skill"));
        SPECIALIZED_SKILL.setDisplayName(new TranslatableComponent("playerskills.quests.specialized_skill"));
    }
}
