package net.impleri.playerskills.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.trinkets.TrinketsSkills

class PlayerSkillsFabric : ModInitializer {
  override fun onInitialize() {
    PlayerSkills.init()
    registerTrinkets()
  }

  private fun registerTrinkets() {
    if (FabricLoader.getInstance().isModLoaded("trinkets")) {
      ServerTickEvents.START_SERVER_TICK.register(
        ServerTickEvents.StartTick { TrinketsSkills.onServerTick(it) },
      )
    }
  }
}
