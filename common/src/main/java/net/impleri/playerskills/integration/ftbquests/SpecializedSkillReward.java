package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.api.Skill;
import net.impleri.playerskills.variant.specialized.SpecializedSkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SpecializedSkillReward extends BasicSkillReward {
    public String value = "";

    public SpecializedSkillReward(Quest quest) {
        super(quest);
    }

    @Override
    protected ResourceLocation getSkillType() {
        return SpecializedSkillType.name;
    }

    @Override
    public RewardType getType() {
        return SkillRewardTypes.SPECIALIZED_SKILL;
    }

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

        config.addEnum("value", value, v -> value = v, NameMap.of(options.get(0), options).create(), actualSkill.getValue())
                .setNameKey("playerskills.quests.ui.value");

    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        if (ServerApi.set(player, skill, value)) {
            maybeNotify(player, notify, value);
        }
    }
}
