package net.impleri.slab.network

import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.simple.SimpleNetworkManager
import net.minecraft.network.FriendlyByteBuf

case class Network(private val underlying: SimpleNetworkManager) {
  def registerMessageToClient[T <: BaseS2CMessage](name: String, factory: FriendlyByteBuf => T): MessageType = {
    underlying.registerS2C(name, b => factory(b))
  }

  def registerMessageToServer[T <: BaseC2SMessage](name: String, factory: FriendlyByteBuf => T): MessageType = {
    underlying.registerC2S(name, b => factory(b))
  }
}

object Network {
  def apply(namespace: String): Network = Network(SimpleNetworkManager.create(namespace))
}
