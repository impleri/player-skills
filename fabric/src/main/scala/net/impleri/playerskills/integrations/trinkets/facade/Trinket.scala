package net.impleri.playerskills.integrations.trinkets.facade

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketComponent
import net.impleri.slab.item.Item

import scala.jdk.CollectionConverters._

case class Trinket(protected val underlying: TrinketComponent) {
  def inventoryMap: Map[SlotReference, Item] = underlying.getAllEquipped
    .asScala
    .toList
    .flatMap(t => Option(t.getB).map(Item(_)).map((t.getA, _)))
    .toMap

  def replace(slot: SlotReference, replacement: Item = Item.DEFAULT_ITEM): Unit = {
    slot.inventory.setItem(slot.index, replacement.getStack)
  }
}
