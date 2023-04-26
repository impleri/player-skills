package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class NumericSkillTask extends BasicSkillTask {
    public double value = 1.0;

    public NumericSkillTask(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return NumericSkillType.name;
    }

    @Override
    public TaskType getType() {
        return SkillTaskTypes.NUMERIC_SKILL;
    }

    @Override
    public long getMaxProgress() {
        return Math.round(value);
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);
        nbt.putDouble("value", value);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);
        value = nbt.getDouble("value");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeDouble(value);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        value = buffer.readDouble();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);

        config.addDouble("value", value, v -> value = v, 1.0, 0.0, Double.MAX_VALUE)
                .setNameKey("playerskills.quests.task.value");
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        return ServerApi.can(player, skill, value);
    }
}
