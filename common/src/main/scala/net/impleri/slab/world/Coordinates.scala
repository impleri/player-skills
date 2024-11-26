package net.impleri.slab.world

import net.minecraft.core.BlockPos

case class Coordinates(
  private val x: Double,
  private val y: Double,
  private val z: Double,
) {
  private def asBlockPos = new BlockPos(x, y, z)

  def canBeChunkCoordinates: Boolean =
    List(x, y, z).forall(v => v >= 0 && v < 16)

  def toPosition: Position = Position(asBlockPos)

  override def toString: String = s"($x, $y, $z)"
}

object Coordinates {
  def apply(x: Double, y: Double, z: Double): Option[Coordinates] =
    for {
      vx <- Option(x)
      vy <- Option(y)
      vz <- Option(z)
    } yield new Coordinates(vx, vy, vz)
}
