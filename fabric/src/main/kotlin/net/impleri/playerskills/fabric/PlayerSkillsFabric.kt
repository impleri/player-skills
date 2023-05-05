package net.impleri.playerskills.fabric

import net.fabricmc.api.ModInitializer
import net.impleri.playerskills.PlayerSkills

class PlayerSkillsFabric : ModInitializer {
  override fun onInitialize() {
    PlayerSkills.init()
  }
}
