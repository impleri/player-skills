package net.impleri.slab.world

import net.minecraft.world.level.BaseSpawner

case class Spawner[T <: BaseSpawner](private val underlying: T)

object Spawner {
  type Any = Spawner[_]
}
