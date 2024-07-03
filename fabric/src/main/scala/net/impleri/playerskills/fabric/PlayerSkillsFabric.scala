package net.impleri.playerskills.fabric

import net.fabricmc.api.ModInitializer
import net.impleri.playerskills.PlayerSkills

case class PlayerSkillsFabric() extends ModInitializer {
  private val INTEGRATIONS: FabricIntegrationLoader = FabricIntegrationLoader(PlayerSkills.STATE)

  override def onInitialize(): Unit = {
    PlayerSkills.init()
  }
}
