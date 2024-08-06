package net.impleri.playerskills.client.bindings

import net.impleri.slab.resources.ReloadListeners
import net.impleri.slab.resources.ResourceManager
import net.impleri.slab.resources.SimpleReloadListener

case class ClientEventBindings(
  callback: Option[ResourceManager] => Unit = _ => {},
  reloadListeners: ReloadListeners = ReloadListeners(),
) extends SimpleReloadListener {
  private[client] def registerEvents(): Unit = {
    reloadListeners.registerClient(this)
  }

  override def onReload(manager: Option[ResourceManager]): Unit = callback(
    manager,
  )
}
