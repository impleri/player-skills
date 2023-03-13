package net.impleri.playerskills.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import net.impleri.playerskills.PlayerSkills;

public abstract class Manager {
    public static SimpleNetworkManager NET = SimpleNetworkManager.create(PlayerSkills.MOD_ID);

    public static MessageType SYNC_SKILLS = NET.registerS2C("sync_skills", SyncSkillsMessage::new);

    public static MessageType RESYNC_SKILLS = NET.registerC2S("resync_skills", ResyncSkillsMessage::new);

    public static void register() {
        PlayerSkills.LOGGER.debug("Registered network channels");
    }
}
