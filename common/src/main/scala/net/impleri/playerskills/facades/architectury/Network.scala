package net.impleri.playerskills.facades.architectury

import dev.architectury.networking.simple.BaseC2SMessage
import dev.architectury.networking.simple.BaseS2CMessage
import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.simple.SimpleNetworkManager
import net.impleri.playerskills.PlayerSkills
import net.minecraft.network.FriendlyByteBuf

case class Network(namespace: String = PlayerSkills.MOD_ID) {
  private val networkManager = SimpleNetworkManager.create(namespace)

  def registerClientboundMessage[T <: BaseS2CMessage](name: String, factory: FriendlyByteBuf => T): MessageType = {
    networkManager.registerS2C(name, b => factory(b))
  }

  def registerServerboundMessage[T <: BaseC2SMessage](name: String, factory: FriendlyByteBuf => T): MessageType = {
    networkManager.registerC2S(name, b => factory(b))
  }
}
