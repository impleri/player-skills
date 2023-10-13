package net.impleri.playerskills.client

import net.impleri.playerskills.network.ResyncSkillsMessage
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.Minecraft

import scala.util.chaining._

object NetHandler {
  def resyncPlayer(): Unit = {
    Minecraft.getInstance()
      .player
      .tap(p => PlayerSkillsLogger.SKILLS.debug(s"Requesting skills resync for ${p.getName}"))
      .pipe(ResyncSkillsMessage(_))
      .pipe(_.sendToServer())
  }
}
