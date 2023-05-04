package net.impleri.playerskills.client

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.network.ResyncSkillsMessage
import net.minecraft.client.Minecraft

internal object NetHandler {
  fun resyncPlayer() {
    val player = Minecraft.getInstance().player ?: return
    PlayerSkills.LOGGER.debug("Requesting skills resync for ${player.name}")
    ResyncSkillsMessage(player).sendToServer()
  }
}
