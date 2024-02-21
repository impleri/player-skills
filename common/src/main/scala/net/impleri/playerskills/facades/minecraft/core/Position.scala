package net.impleri.playerskills.facades.minecraft.core

import net.minecraft.core.BlockPos

case class Position(private val pos: BlockPos) {
  val raw: BlockPos = pos
}

object Position {
  def empty: Option[Position] = None
}
