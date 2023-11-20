package net.impleri.playerskills.facades.architectury

import dev.architectury.registry.ReloadListenerRegistry
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener

case class ReloadListeners(private val isStub: Boolean = false) {
  def register(
    listener: PreparableReloadListener,
    packType: PackType = PackType.SERVER_DATA,
  ): Unit = {
    if (!isStub) {
      ReloadListenerRegistry.register(packType, listener)
    }
  }
}

object ReloadListeners {
  def apply(stub: Boolean = false): ReloadListeners = new ReloadListeners(stub)
}
