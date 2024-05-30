package net.impleri.slab.menu

import net.minecraft.world.inventory.AbstractContainerMenu

case class ContainerMenu[T <: AbstractContainerMenu](private val underlying: T) {
  def getId: Int = underlying.containerId

  def getNextStateId: Int = underlying.incrementStateId()
}

object ContainerMenu {
  type Any = ContainerMenu[AbstractContainerMenu]

  def fromVanilla[T <: AbstractContainerMenu](underlying: T): Option[ContainerMenu[T]] = {
    Option(underlying).map(ContainerMenu(_))
  }
}
