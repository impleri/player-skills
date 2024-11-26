package net.impleri.slab.client

import net.minecraft.client.Minecraft

case class Client() {
  def getInstance: Minecraft = Minecraft.getInstance()

  def getPlayer: Option[Player] =
    Option(getInstance)
      .map(_.player)
      .flatMap(Option(_))
      .map(Player(_))
}
