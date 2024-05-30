package net.impleri.slab.resources

import net.minecraft.server.packs.resources.{ResourceManager => McResourceManager}

import scala.util.chaining.scalaUtilChainingOps

trait SimpleReloadListener extends ReloadListener {
  protected def onReload(manager: Option[ResourceManager]): Unit

  override def onResourceManagerReload(resourceManager: McResourceManager): Unit = {
    Option(resourceManager)
      .map(ResourceManager)
      .pipe(onReload)
  }
}
