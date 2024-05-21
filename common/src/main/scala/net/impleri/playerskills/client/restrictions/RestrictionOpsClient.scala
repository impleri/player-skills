package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.client.api.ClientPlayer
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.client.player.LocalPlayer

trait RestrictionOpsClient {
  protected def client: Client

  protected def getPlayer: Option[Player[LocalPlayer]] = client.getPlayer

  protected def maybeCan(f: Player[LocalPlayer] => Boolean): Boolean = {
    getPlayer
      .fold(ClientPlayer.DEFAULT_SKILL_RESPONSE)(f)
  }
}
