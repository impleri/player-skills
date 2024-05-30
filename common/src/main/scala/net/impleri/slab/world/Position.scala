package net.impleri.slab.world

import net.minecraft.core.BlockPos

case class Position(private val underlying: BlockPos) {
  val raw: BlockPos = underlying

  def asCoordinates: Option[Coordinates] = Coordinates(underlying.getX, underlying.getY, underlying.getZ)

  override def toString: String = asCoordinates.fold("")(_.toString)
}

object Position {
  def empty: Option[Position] = None
}
