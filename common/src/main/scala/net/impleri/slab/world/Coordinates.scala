package net.impleri.slab.world

import net.minecraft.core.BlockPos

case class Coordinates(private val x: Double, private val y: Double, private val z: Double) {
  private def asBlockPos = new BlockPos(x, y, z)

  def canBeChunkCoordinates: Boolean = List(x, y, z).forall(v => v >= 0 && v < 16)

  def toPosition: Position = Position(asBlockPos)

  override def toString: String = s"($x, $y, $z)"
}

object Coordinates {
  def apply(x: Double, y: Double, z: Double): Option[Coordinates] = {
    Option(x)
      .flatMap(i => Option(y).map((i, _)))
      .flatMap(t => Option(z).map((t._1, t._2, _)))
      .map(t => new Coordinates(t._1, t._2, t._3))
  }
}
