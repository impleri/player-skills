package net.impleri.playerskills.facades.minecraft

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

case class Client() {
  def getInstance: Minecraft = Minecraft.getInstance()

  def getPlayer: Option[Player[LocalPlayer]] = {
    Option(getInstance)
      .map(_.player)
      .flatMap(Option(_))
      .map(Player(_))
  }
}
