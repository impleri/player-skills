package net.impleri.slab.world

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

case class Coordinates(
  x: Double,
  y: Double,
  z: Double,
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

  def apply(blockPos: BlockPos) = new Coordinates(blockPos.getX, blockPos.getY, blockPos.getZ)

  def apply(vec: Vec3) = new Coordinates(vec.x, vec.y, vec.z)

  type Vanilla = Vec3
}
