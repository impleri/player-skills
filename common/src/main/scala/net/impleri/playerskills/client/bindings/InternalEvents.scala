package net.impleri.playerskills.client.bindings

import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.server.packs.PackType

case class InternalEvents(
  onReload: ResourceManager => Unit = _ => {},
  reloadListeners: ReloadListeners = ReloadListeners(),
)
  extends ResourceManagerReloadListener {
  private[client] def registerEvents(): Unit = {
    reloadListeners.register(this, PackType.CLIENT_RESOURCES)
  }

  override def onResourceManagerReload(resourceManager: ResourceManager): Unit = onReload(resourceManager)
}
