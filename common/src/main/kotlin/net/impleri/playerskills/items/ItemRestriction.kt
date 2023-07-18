package net.impleri.playerskills.items

import net.impleri.playerskills.restrictions.AbstractRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

open class ItemRestriction(
  item: Item,
  condition: (Player) -> Boolean,
  replacement: Item? = null,
  includeDimensions: List<ResourceLocation>? = null,
  excludeDimensions: List<ResourceLocation>? = null,
  includeBiomes: List<ResourceLocation>? = null,
  excludeBiomes: List<ResourceLocation>? = null,
  producible: Boolean? = null,
  consumable: Boolean? = null,
  holdable: Boolean? = null,
  identifiable: Boolean? = null,
  harmful: Boolean? = null,
  wearable: Boolean? = null,
  usable: Boolean? = null,
) : AbstractRestriction<Item>(
  item,
  condition,
  includeDimensions ?: ArrayList(),
  excludeDimensions ?: ArrayList(),
  includeBiomes ?: ArrayList(),
  excludeBiomes ?: ArrayList(),
  replacement,
) {
  val producible = producible ?: false
  val consumable = consumable ?: false
  val holdable = holdable ?: false
  val identifiable = identifiable ?: false
  val harmful = harmful ?: false
  val wearable = wearable ?: false
  val usable = usable ?: false
}
