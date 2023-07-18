package net.impleri.playerskills.items

import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

interface ItemConditions<Player> : RestrictionConditionsBuilder<Item, Player, ItemRestriction> {
  var replacement: Item?
  var producible: Boolean?
  var consumable: Boolean?
  var identifiable: Boolean?
  var holdable: Boolean?
  var wearable: Boolean?
  var usable: Boolean?
  var harmful: Boolean?

  // Not yet!
  private fun replaceWith(replacement: ResourceLocation): ItemConditions<Player> {
    this.replacement = ItemRestrictions.getValue(replacement)
    return this
  }

  fun producible(): ItemConditions<Player> {
    producible = true
    holdable = true
    return this
  }

  fun unproducible(): ItemConditions<Player> {
    producible = false
    return this
  }

  fun consumable(): ItemConditions<Player> {
    consumable = true
    holdable = true
    return this
  }

  fun unconsumable(): ItemConditions<Player> {
    consumable = false
    return this
  }

  fun holdable(): ItemConditions<Player> {
    holdable = true
    return this
  }

  fun unholdable(): ItemConditions<Player> {
    holdable = false
    producible = false
    consumable = false
    harmful = false
    wearable = false
    usable = false
    return this
  }

  fun identifiable(): ItemConditions<Player> {
    identifiable = true
    return this
  }

  fun unidentifiable(): ItemConditions<Player> {
    identifiable = false
    return this
  }

  fun harmful(): ItemConditions<Player> {
    harmful = true
    holdable = true
    return this
  }

  fun harmless(): ItemConditions<Player> {
    harmful = false
    return this
  }

  fun wearable(): ItemConditions<Player> {
    wearable = true
    holdable = true
    return this
  }

  fun unwearable(): ItemConditions<Player> {
    wearable = false
    return this
  }

  fun usable(): ItemConditions<Player> {
    usable = true
    holdable = true
    return this
  }

  fun unusable(): ItemConditions<Player> {
    usable = false
    return this
  }

  fun nothing(): ItemConditions<Player> {
    producible = true
    consumable = true
    holdable = true
    identifiable = true
    harmful = true
    wearable = true
    usable = true
    return this
  }

  fun everything(): ItemConditions<Player> {
    producible = false
    consumable = false
    holdable = false
    identifiable = false
    harmful = false
    wearable = false
    usable = false
    return this
  }
}
