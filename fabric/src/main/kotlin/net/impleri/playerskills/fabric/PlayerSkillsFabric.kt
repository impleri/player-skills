package net.impleri.playerskills.fabric

import net.fabricmc.api.ModInitializer
import net.impleri.playerskills.PlayerSkills.init

class PlayerSkillsFabric : ModInitializer {
  override fun onInitialize() {
    init()
  }
}
