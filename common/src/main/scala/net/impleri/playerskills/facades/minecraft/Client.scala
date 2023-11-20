package net.impleri.playerskills.facades.minecraft

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

case class Client() {
  def getInstace: Minecraft = Minecraft.getInstance()

  def getPlayer: Player[LocalPlayer] = Player(getInstace.player)
}
