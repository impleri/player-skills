package net.impleri.playerskills.network;

import dev.architectury.event.EventResult;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.api.ServerApi;
import net.impleri.playerskills.events.SkillChangedEvent;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetHandler {
    public static SimpleNetworkManager NET = SimpleNetworkManager.create(PlayerSkillsCore.MOD_ID);

    public static MessageType SYNC_SKILLS = NET.registerS2C("sync_skills", SyncSkillsMessage::new);

    public static EventResult syncPlayer(SkillChangedEvent<?> event) {
        var player = event.getPlayer();

        if (player instanceof ServerPlayer serverPlayer) {
            syncPlayer(serverPlayer);
        } else {
            PlayerSkillsCore.LOGGER.warn("Attempted to sync skill changes from clientside");
        }

        return EventResult.pass();
    }

    public static void syncPlayer(ServerPlayer player) {
        PlayerSkillsCore.LOGGER.info("Syncing player skills to {}", player.getName().getString());
        new SyncSkillsMessage(player, ServerApi.getAllSkills(player)).sendTo(player);
    }
}
