package net.impleri.slab.resources

import dev.architectury.registry.ReloadListenerRegistry

case class ReloadListeners(private val isStub: Boolean = false) {
  def register(
    listener: ReloadListener,
    identifier: Option[ResourceLocation] = None,
    listenerType: ListenerType.ListenerType = ListenerType.Server,
  ): Unit = {
    if (!isStub) {
      ReloadListenerRegistry.register(
        listenerType.asPack,
        listener,
        identifier.map(_.value).orNull,
      )
    }
  }

  def registerClient(
    listener: ReloadListener,
    identifier: Option[ResourceLocation] = None,
  ): Unit = {
    register(
      listener,
      identifier,
      ListenerType.Client,
    )
  }

  def registerServer(
    listener: ReloadListener,
    identifier: Option[ResourceLocation] = None,
  ): Unit = {
    register(
      listener,
      identifier,
      ListenerType.Server,
    )
  }
}
