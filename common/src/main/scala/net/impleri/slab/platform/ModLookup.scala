package net.impleri.slab.platform

import dev.architectury.platform.Platform

trait ModLookup {
  protected def isModLoaded(mod: String): Boolean = {
    Platform.isModLoaded(mod)
  }
}
