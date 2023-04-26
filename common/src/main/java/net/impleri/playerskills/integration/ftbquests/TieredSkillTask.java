package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.api.Skill;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TieredSkillTask extends BasicSkillTask {
    public String value;

    public TieredSkillTask(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return TieredSkillType.name;
    }

    @Override
    public TaskType getType() {
        return SkillTaskTypes.TIERED_SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putString("value", value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        value = nbt.getString("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(value, Short.MAX_VALUE);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = buffer.readUtf(Short.MAX_VALUE);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);

        net.impleri.playerskills.api.Skill<String> actualSkill;
        try {
            actualSkill = Skill.find(skill);
        } catch (RegistryItemNotFound e) {
            throw new RuntimeException(e);
        }

        var options = actualSkill.getOptions();

        if (!actualSkill.isAllowedValue(value)) {
            value = options.get(0);
        }

        config.addEnum("value", value, v -> value = v, NameMap.of(options.get(0), options).create(), actualSkill.getValue())
                .setNameKey("playerskills.quests.task.value");
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        return ServerApi.can(player, skill, value);
    }
}
