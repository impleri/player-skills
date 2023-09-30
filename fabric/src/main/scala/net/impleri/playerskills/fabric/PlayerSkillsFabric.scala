package net.impleri.playerskills.fabric

import net.fabricmc.api.ModInitializer
import net.impleri.playerskills.PlayerSkills

case class PlayerSkillsFabric() extends ModInitializer {
  override def onInitialize(): Unit = {
    PlayerSkills.init()
    //    registerTrinkets()
  }

//  private def registerTrinkets(): Unit = {
//    if (FabricLoader.getInstance().isModLoaded("trinkets")) {
//      ServerTickEvents.START_SERVER_TICK.register(s => TrinketsSkills.onServerTick(s))
//    }
//  }
}
