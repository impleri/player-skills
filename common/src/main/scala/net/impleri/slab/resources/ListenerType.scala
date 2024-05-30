package net.impleri.slab.resources

import net.minecraft.server.packs.PackType

object ListenerType extends Enumeration {
  case class ListenerType(packType: PackType) extends super.Val {
    def asPack: PackType = packType
  }

  final val Client: ListenerType = ListenerType(PackType.CLIENT_RESOURCES)
  final val Server: ListenerType = ListenerType(PackType.SERVER_DATA)
}
