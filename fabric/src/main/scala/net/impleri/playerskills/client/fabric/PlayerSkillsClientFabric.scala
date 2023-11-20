package net.impleri.playerskills.client.fabric

import net.fabricmc.api.ClientModInitializer
import net.impleri.playerskills.client.PlayerSkillsClient

case class PlayerSkillsClientFabric() extends ClientModInitializer {
  override def onInitializeClient(): Unit = {
    PlayerSkillsClient.resyncSkills()
  }
}
