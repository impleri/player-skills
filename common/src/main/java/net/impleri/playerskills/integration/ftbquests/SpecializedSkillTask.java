package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.minecraft.resources.ResourceLocation;

public class SpecializedSkillTask extends TieredSkillTask {
    public SpecializedSkillTask(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return SpecializedSkillType.name;
    }

    @Override
    public TaskType getType() {
        return SkillTaskTypes.SPECIALIZED_SKILL;
    }
}
