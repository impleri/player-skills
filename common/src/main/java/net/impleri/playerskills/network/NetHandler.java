package net.impleri.playerskills.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetHandler {
    public static SimpleNetworkManager NET = SimpleNetworkManager.create(PlayerSkills.MOD_ID);

    public static MessageType SYNC_SKILLS = NET.registerS2C("sync_skills", SyncSkillsMessage::new);

    public static MessageType RESYNC_SKILLS = NET.registerC2S("resync_skills", ResyncSkillsMessage::new);

    public static void syncPlayer(SkillChangedEvent<?> event) {
        var player = event.getPlayer();

        if (player instanceof ServerPlayer serverPlayer) {
            syncPlayer(serverPlayer);
        } else {
            PlayerSkills.LOGGER.warn("Attempted to sync skill changes from clientside");
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to sync skill changes for nobody");
            return;
        }

        var skills = ServerApi.getAllSkills(player);
        PlayerSkills.LOGGER.debug("Syncing {} player skills to {}", skills.size(), player.getName().getString());

        new SyncSkillsMessage(player, skills).sendTo(player);
    }

    public static void resyncPlayer(LocalPlayer player) {
        PlayerSkills.LOGGER.debug("Requesting skills resync for {}", player.getName().getString());

        new ResyncSkillsMessage(player).sendToServer();
    }
}
