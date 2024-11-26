package net.impleri.slab.client

import net.impleri.slab.entity.{Player => PlayerEntity}
import net.impleri.slab.network.ServerboundMessage
import net.minecraft.client.player.LocalPlayer

case class Player(override val underlying: Player.Vanilla)
    extends PlayerEntity(underlying) {
  def sendMessage(message: ServerboundMessage): Unit =
    if (isClient) {
      message.sendToServer()
    }
}

object Player {
  type Vanilla = LocalPlayer

  def fromVanilla(underlying: LocalPlayer): Option[Player] =
    Option(underlying).map(Player(_))
}
