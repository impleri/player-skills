package net.impleri.playerskills.forge

import dev.architectury.platform.forge.EventBuses
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.curios.CuriosSkills
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(PlayerSkills.MOD_ID)
class PlayerSkillsForge {
  init {
    // Submit our event bus to let architectury register our content on the right time
    EventBuses.registerModEventBus(PlayerSkills.MOD_ID, FMLJavaModLoadingContext.get().modEventBus)
    PlayerSkills.init()
  }

  @SubscribeEvent
  fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
    if (event.side == LogicalSide.SERVER && ModList.get().isLoaded("curios")) {
      CuriosSkills.onPlayerTick(event.player)
    }
  }
}
