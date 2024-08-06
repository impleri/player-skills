package net.impleri.slab.entity

import net.minecraft.world.entity.animal.{Animal => McAnimal}

case class Animal[T <: Animal.Vanilla](override val underlying: T)
    extends Entity(underlying)

object Animal {
  type Any = Animal[_]

  type Vanilla = McAnimal
}
