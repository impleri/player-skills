package net.impleri.playerskills.server.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.impleri.playerskills.server.PlayerSkillsServer;

public class PlayerSkillsServerFabric implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        PlayerSkillsServer.init();
    }
}
