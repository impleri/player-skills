package net.impleri.playerskills.integration.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.impleri.playerskills.variant.basic.BasicSkillType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicSkillReward extends Reward {
    public ResourceLocation skill;
    public boolean downgrade = false;

    public BasicSkillReward(Quest quest) {
        super(quest);
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    protected ResourceLocation getSkillType() {
        return BasicSkillType.name;
    }

    @Override
    public RewardType getType() {
        return SkillRewardTypes.BASIC_SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt) {
        super.writeData(nbt);

        nbt.putString("skill", skill.toString());

        if (downgrade) {
            nbt.putBoolean("downgrade", true);
        }
    }

    @Override
    public void readData(CompoundTag nbt) {
        super.readData(nbt);

        skill = SkillResourceLocation.of(nbt.getString("skill"));
        downgrade = nbt.getBoolean("downgrade");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);

        buffer.writeUtf(skill.toString(), Short.MAX_VALUE);
        buffer.writeBoolean(downgrade);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);

        skill = SkillResourceLocation.of(buffer.readUtf(Short.MAX_VALUE));
        downgrade = buffer.readBoolean();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void getConfig(ConfigGroup config) {
        super.getConfig(config);

        List<ResourceLocation> skills = PlayerSkillsIntegration.getSkills(getSkillType());

        var firstSkill = skills.get(0);
        if (skill == null) {
            skill = firstSkill;
        }

        config.addEnum(
                "skill",
                skill,
                v -> skill = v,
                NameMap.of(firstSkill, skills).create(),
                firstSkill
        ).setNameKey("playerskills.quests.ui.skill");

        config.addBool("downgrade", downgrade, v -> downgrade = v, false).setNameKey("playerskills.quests.ui.downgrade");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MutableComponent getAltTitle() {
        return new TranslatableComponent("playerskills.quests.ui.skill").append(": ").append(new TextComponent(skill.toString()).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean ignoreRewardBlocking() {
        return true;
    }

    @Override
    protected boolean isIgnoreRewardBlockingHardcoded() {
        return true;
    }

    protected void maybeNotify(ServerPlayer player, boolean notify, @Nullable String value) {
        if (notify) {
            var messageKey = downgrade ? "playerskills.quests.reward.downgrade" : "playerskills.quests.reward.upgrade";
            if (value != null) {
                messageKey += "_value";
            }

            player.sendMessage(new TranslatableComponent(messageKey, skill, value), player.getUUID());
        }
    }

    protected void maybeNotify(ServerPlayer player, boolean notify) {
        maybeNotify(player, notify, null);
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        if (ServerApi.set(player, skill, !downgrade)) {
            maybeNotify(player, notify);
        }
    }
}
