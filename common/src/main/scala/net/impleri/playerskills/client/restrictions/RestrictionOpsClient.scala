package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.client.api.ClientPlayer
import net.impleri.slab.client.Client
import net.impleri.slab.client.Player

trait RestrictionOpsClient {
  protected def client: Client

  protected def getPlayer: Option[Player] = client.getPlayer

  protected def maybeCan(f: Player => Boolean): Boolean =
    getPlayer
      .fold(ClientPlayer.DEFAULT_SKILL_RESPONSE)(f)
}
