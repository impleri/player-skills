package net.impleri.playerskills.integration.ftbquests;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.impleri.playerskills.server.api.Skill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PlayerSkillsIntegration {
    public static void init() {
        SkillTaskTypes.init();
        SkillRewardTypes.init();
    }

    public static List<ResourceLocation> getSkills(ResourceLocation skillType) {
        return Skill.stream()
                .filter(skill -> skill.getType() == skillType)
                .map(net.impleri.playerskills.api.Skill::getName)
                .toList();
    }

    public static void checkStages(ServerPlayer player) {
        TeamData data = ServerQuestFile.INSTANCE == null || PlayerHooks.isFake(player) ? null : ServerQuestFile.INSTANCE.getData(player);

        if (data == null || data.isLocked()) {
            return;
        }

        ServerQuestFile.INSTANCE.withPlayerContext(player, () -> {
            for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
                if (task instanceof BasicSkillTask && data.canStartTasks(task.quest)) {
                    task.submitTask(data, player);
                }
            }
        });
    }
}
