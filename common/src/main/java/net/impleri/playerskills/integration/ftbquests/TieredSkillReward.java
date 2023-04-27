package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.api.Skill;
import net.impleri.playerskills.variant.tiered.TieredSkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class TieredSkillReward extends BasicSkillReward {
    public static String NO_VALUE = "";
    public String min = NO_VALUE;
    public String max = NO_VALUE;

    public TieredSkillReward(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return TieredSkillType.name;
    }

    @Override
    public RewardType getType() {
        return SkillRewardTypes.TIERED_SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putString("min", min);
        nbt.putString("max", max);
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        min = nbt.getString("min");
        max = nbt.getString("max");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeUtf(min, Short.MAX_VALUE);
        buffer.writeUtf(max, Short.MAX_VALUE);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);

        min = buffer.readUtf(Short.MAX_VALUE);
        max = buffer.readUtf(Short.MAX_VALUE);
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

        var options = new ArrayList<>(actualSkill.getOptions());
        options.add(0, NO_VALUE);

        if (!actualSkill.isAllowedValue(min)) {
            min = NO_VALUE;
        }

        if (!actualSkill.isAllowedValue(max)) {
            max = NO_VALUE;
        }

        config.addEnum("min", min, v -> min = v, NameMap.of(NO_VALUE, options).create(), actualSkill.getValue())
                .setNameKey("playerskills.quests.ui.min");
        config.addEnum("max", max, v -> max = v, NameMap.of(NO_VALUE, options).create(), actualSkill.getValue())
                .setNameKey("playerskills.quests.ui.min");

    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        net.impleri.playerskills.api.Skill<String> actualSkill;
        SkillType<String> skillType;
        String nextVal;

        try {
            actualSkill = Skill.find(skill);
            skillType = SkillType.find(getSkillType());
        } catch (RegistryItemNotFound e) {
            throw new RuntimeException(e);
        }

        nextVal = skillType.getNextValue(actualSkill, min.isEmpty() ? null : min, max.isEmpty() ? null : max);
        if (downgrade) {
            nextVal = skillType.getPrevValue(actualSkill, min.isEmpty() ? null : min, max.isEmpty() ? null : max);
        }

        if (nextVal != null && ServerApi.set(player, skill, nextVal)) {
            maybeNotify(player, notify, nextVal);
        }
    }
}
