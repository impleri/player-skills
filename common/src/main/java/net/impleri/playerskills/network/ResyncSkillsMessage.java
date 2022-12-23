package net.impleri.playerskills.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.impleri.playerskills.PlayerSkills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ResyncSkillsMessage extends BaseC2SMessage {
    private final UUID playerId;

    ResyncSkillsMessage(FriendlyByteBuf buffer) {
        playerId = buffer.readUUID();
    }

    public ResyncSkillsMessage(Player player) {
        playerId = player.getUUID();
    }

    @Override
    public MessageType getType() {
        return Manager.RESYNC_SKILLS;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        PlayerSkills.resync(playerId);
    }
}
