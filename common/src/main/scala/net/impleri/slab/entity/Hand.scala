package net.impleri.slab.entity

import net.minecraft.world.InteractionHand

object Hand extends Enumeration {
  case class Hand(underlying: InteractionHand) extends super.Val

  final val MAIN_HAND: Hand = Hand(InteractionHand.MAIN_HAND)
  final val OFF_HAND: Hand = Hand(InteractionHand.OFF_HAND)

  def fromVanilla(hand: InteractionHand): Hand = {
    hand match {
      case InteractionHand.OFF_HAND => OFF_HAND
      case _ => MAIN_HAND
    }
  }
}
