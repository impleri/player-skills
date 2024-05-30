package net.impleri.slab.entity

import net.minecraft.world.entity.animal.{Animal => McAnimal}

case class Animal[T <: McAnimal](private val underlying: T) extends Entity(underlying)

object Animal {
  type Any = Animal[_]
}
