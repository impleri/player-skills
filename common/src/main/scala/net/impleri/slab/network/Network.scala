package net.impleri.slab.network

import dev.architectury.networking.simple.SimpleNetworkManager
import dev.architectury.networking.NetworkManager

case class Network(private val underlying: SimpleNetworkManager) {
  def registerMessageToClient[T <: ClientboundMessage.Vanilla](
    name: String,
    factory: FriendlyBuffer.Vanilla => T,
  ): MessageType.Vanilla =
    underlying.registerS2C(name, b => factory(b))

  def registerMessageToServer[T <: ServerboundMessage.Vanilla](
    name: String,
    factory: FriendlyBuffer.Vanilla => T,
  ): MessageType.Vanilla =
    underlying.registerC2S(name, b => factory(b))
}

object Network {
  type Context = NetworkManager.PacketContext

  def apply(namespace: String): Network = Network(
    SimpleNetworkManager.create(namespace),
  )
}
