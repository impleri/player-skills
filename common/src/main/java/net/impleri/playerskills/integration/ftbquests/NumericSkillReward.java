package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.api.Skill;
import net.impleri.playerskills.variant.numeric.NumericSkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class NumericSkillReward extends BasicSkillReward {
    protected static double NO_VALUE = -1.0;
    public double min = NO_VALUE;
    public double max = NO_VALUE;

    public NumericSkillReward(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return NumericSkillType.name;
    }

    @Override
    public RewardType getType() {
        return SkillRewardTypes.NUMERIC_SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putDouble("min", min);
        nbt.putDouble("max", max);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        min = nbt.getDouble("min");
        max = nbt.getDouble("max");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeDouble(min);
        buffer.writeDouble(max);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);

        min = buffer.readDouble();
        max = buffer.readDouble();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);

        config.addDouble("min", min, v -> min = v, NO_VALUE, NO_VALUE, Double.MAX_VALUE)
                .setNameKey("playerskills.quests.ui.min");

        config.addDouble("max", max, v -> max = v, NO_VALUE, NO_VALUE, Double.MAX_VALUE)
                .setNameKey("playerskills.quests.ui.max");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        net.impleri.playerskills.api.Skill<Double> actualSkill;
        SkillType<Double> skillType;
        Double nextVal;

        try {
            actualSkill = Skill.find(skill);
            skillType = SkillType.find(getSkillType());
        } catch (RegistryItemNotFound e) {
            throw new RuntimeException(e);
        }

        nextVal = skillType.getNextValue(actualSkill, min < 0 ? null : min, max < 0 ? null : max);
        if (downgrade) {
            nextVal = skillType.getPrevValue(actualSkill, min < 0 ? null : min, max < 0 ? null : max);
        }

        if (nextVal != null && ServerApi.set(player, skill, nextVal)) {
            maybeNotify(player, notify, nextVal.toString());
        }
    }
}
