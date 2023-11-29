package net.impleri.playerskills.server.fabric

import net.fabricmc.api.DedicatedServerModInitializer
import net.impleri.playerskills.server.PlayerSkillsServer

case class PlayerSkillsServerFabric() extends DedicatedServerModInitializer {
  override def onInitializeServer(): Unit = {
    PlayerSkillsServer.init()
  }
}
