package net.impleri.slab.world

import net.minecraft.core.{Direction => McDirection}

object Direction extends Enumeration {
  case class Direction(private val underlying: McDirection) extends super.Val

  val DOWN: Direction = Direction(McDirection.DOWN)
  val UP: Direction = Direction(McDirection.UP)
  val NORTH: Direction = Direction(McDirection.NORTH)
  val SOUTH: Direction = Direction(McDirection.SOUTH)
  val EAST: Direction = Direction(McDirection.EAST)
  val WEST: Direction = Direction(McDirection.WEST)

  def fromVanilla(value: McDirection): Direction = {
    value match {
      case McDirection.UP => UP
      case McDirection.NORTH => NORTH
      case McDirection.SOUTH => SOUTH
      case McDirection.EAST => EAST
      case McDirection.WEST => WEST
      case _ => DOWN
    }
  }
}
