package net.impleri.playerskills.network;

import com.google.common.collect.ImmutableList;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.client.PlayerSkillsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncSkillsMessage extends BaseS2CMessage {
    private final UUID playerId;
    private final List<Skill<?>> skills;

    SyncSkillsMessage(FriendlyByteBuf buffer) {
        playerId = buffer.readUUID();

        int size = buffer.readInt();
        skills = new ArrayList<>(size);

        PlayerSkills.LOGGER.debug("Received skill sync of {} skills for {}", size, playerId);

        for (int i = 0; i < size; i++) {
            var stringSize = buffer.readInt();
            var string = buffer.readUtf(stringSize);

            var rawSkill = SkillType.unserializeFromString(string);
            if (rawSkill != null) {
                skills.add(rawSkill);
            }
        }
    }

    public SyncSkillsMessage(Player player, List<Skill<?>> skills) {
        playerId = player.getUUID();
        this.skills = skills;
    }

    @Override
    public MessageType getType() {
        return Manager.SYNC_SKILLS;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);

        var size = skills.size();
        buffer.writeInt(size);

        for (Skill<?> skill : skills) {
            var string = SkillType.serializeToString(skill);
            var stringSize = string.length();

            buffer.writeInt(stringSize);
            buffer.writeUtf(string, stringSize);
        }

        PlayerSkills.LOGGER.debug("Sending skill sync of {} skills for {}", size, playerId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        PlayerSkillsClient.syncFromServer(ImmutableList.copyOf(skills));
    }
}
