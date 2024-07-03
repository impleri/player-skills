package net.impleri.playerskills.integrations.curios.facade

import net.impleri.slab.item.Item
import net.minecraftforge.items.IItemHandlerModifiable

case class Curio(private val underlying: IItemHandlerModifiable) {
  def inventoryMap: Map[Int, Item] = {
    underlying.getSlots match {
      case n if n > 0 => (0 to n).flatMap(n => Option(underlying.getStackInSlot(n))
        .map(Item(_))
        .filterNot(_.isEmpty)
        .map((n, _)),
      ).toMap
      case _ => Map.empty
    }
  }

  def removeItem(slot: Int, amount: Int): Option[Item] = {
    Option(underlying.extractItem(slot, amount, false))
      .map(Item(_))
      .filterNot(_.isEmpty)
  }
}
