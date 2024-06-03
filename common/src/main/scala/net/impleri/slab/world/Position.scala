package net.impleri.slab.world

import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.core.BlockPos

case class Position(override protected val underlying: BlockPos) extends ResourceWrapper[BlockPos] {
  override def name: Option[ResourceLocation] = None

  override val value: BlockPos = underlying

  def asCoordinates: Option[Coordinates] = Coordinates(underlying.getX, underlying.getY, underlying.getZ)

  override def toString: String = asCoordinates.fold("")(_.toString)
}

object Position {
  def empty: Option[Position] = None
}
