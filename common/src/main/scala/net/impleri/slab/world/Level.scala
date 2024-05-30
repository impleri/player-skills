package net.impleri.slab.world

import net.minecraft.world.level.LevelAccessor

case class Level[T <: LevelAccessor](private val underlying: T)

object Level {
  type Any = Level[_]
}
