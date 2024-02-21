package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.client.player.LocalPlayer

trait RestrictionOpsClient {
  protected def client: Client

  protected def getPlayer: Player[LocalPlayer] = client.getPlayer
}
