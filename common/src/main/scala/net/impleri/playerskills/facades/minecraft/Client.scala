package net.impleri.playerskills.facades.minecraft

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

case class Client() {
  def getInstance: Minecraft = Minecraft.getInstance()

  def getPlayer: Player[LocalPlayer] = Player(getInstance.player)
}
