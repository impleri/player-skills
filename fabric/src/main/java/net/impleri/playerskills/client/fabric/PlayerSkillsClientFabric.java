package net.impleri.playerskills.client.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.impleri.playerskills.client.PlayerSkillsClient;

public class PlayerSkillsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PlayerSkillsClient.init();
    }
}
