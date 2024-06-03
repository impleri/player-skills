package net.impleri.slab.menu

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.inventory.AbstractContainerMenu

case class ContainerMenu[T <: ContainerMenu.Vanilla](override val underlying: T) extends ResourceWrapper[T] {
  def getId: Int = underlying.containerId

  def getNextStateId: Int = underlying.incrementStateId()

  override val name: Option[ResourceLocation] = None
}

object ContainerMenu {
  type Vanilla = AbstractContainerMenu

  type Any = ContainerMenu[Vanilla]


  def fromVanilla[T <: Vanilla](underlying: T): Option[ContainerMenu[T]] = {
    Option(underlying).map(ContainerMenu(_))
  }
}
