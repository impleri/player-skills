package net.impleri.playerskills.client.fabric

import net.fabricmc.api.ClientModInitializer
import net.impleri.playerskills.client.PlayerSkillsClient

class PlayerSkillsClientFabric : ClientModInitializer {
  override fun onInitializeClient() {
    PlayerSkillsClient.init()
  }
}
