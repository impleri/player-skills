package net.impleri.playerskills.server;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.network.SyncSkillsMessage;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetHandler {
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
}
