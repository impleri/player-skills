package net.impleri.slab.client

import net.impleri.slab.entity.Player
import net.minecraft.client.Minecraft

case class Client() {
  def getInstance: Minecraft = Minecraft.getInstance()

  def getPlayer: Option[Player.Local] = {
    Option(getInstance)
      .map(_.player)
      .flatMap(Option(_))
      .map(Player(_))
  }
}
