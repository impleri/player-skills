package net.impleri.playerskills.forge

import dev.architectury.platform.forge.EventBuses
import net.impleri.playerskills.PlayerSkills
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(PlayerSkills.MOD_ID)
object PlayerSkillsForge {
    // Submit our event bus to let architectury register our content on the right time
    EventBuses.registerModEventBus(PlayerSkills.MOD_ID, FMLJavaModLoadingContext.get.getModEventBus)
    PlayerSkills.init()

//  @SubscribeEvent
//  def onPlayerTick(event: TickEvent.PlayerTickEvent) = {
//    if (event.side == LogicalSide.SERVER && ModList.get().isLoaded("curios")) {
//      CuriosSkills.onPlayerTick(event.player)
//    }
//  }
}
