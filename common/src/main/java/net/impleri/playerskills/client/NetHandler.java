package net.impleri.playerskills.client;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.network.ResyncSkillsMessage;
import net.minecraft.client.Minecraft;

abstract class NetHandler {
    public static void resyncPlayer() {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        PlayerSkills.LOGGER.debug("Requesting skills resync for {}", player.getName().getString());

        new ResyncSkillsMessage(player).sendToServer();
    }
}
