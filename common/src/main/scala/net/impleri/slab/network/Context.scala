package net.impleri.slab.network

import dev.architectury.networking.NetworkManager
import dev.architectury.utils.Env
import net.impleri.slab.entity.Player

import scala.util.chaining.scalaUtilChainingOps

case class Context (private val underlying: Context.Vanilla) {
  def player: Option[Player] = underlying
      .getPlayer
      .pipe(Option(_))
      .map(Player(_))

  private def env: Env = underlying.getEnvironment

  def isClient: Boolean = env == Env.CLIENT

  def isServer: Boolean = env == Env.SERVER
}

object Context {
  type Vanilla = NetworkManager.PacketContext
}
