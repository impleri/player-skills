package net.impleri.playerskills.client

import net.impleri.playerskills.network.ResyncSkillsMessage
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.Minecraft

internal object NetHandler {
  fun resyncPlayer() {
    val player = Minecraft.getInstance().player ?: return
    PlayerSkillsLogger.SKILLS.debug("Requesting skills resync for ${player.name}")
    ResyncSkillsMessage(player).sendToServer()
  }
}
