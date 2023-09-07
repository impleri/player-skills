package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.items.ItemConditions
import net.impleri.playerskills.restrictions.items.ItemRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

class ItemRestrictionConditionBuilder(name: ResourceLocation) :
  AbstractRestrictionConditionBuilder<Item, ItemRestriction>(name),
  ItemConditions<Player>,
  RestrictionDataParser {
  override var replacement: Item? = null
  override var producible: Boolean? = null
  override var consumable: Boolean? = null
  override var identifiable: Boolean? = null
  override var holdable: Boolean? = null
  override var wearable: Boolean? = null
  override var usable: Boolean? = true
  override var harmful: Boolean? = null

  override fun parseRestriction(
    jsonElement: JsonObject,
  ) {
    parseTarget(jsonElement, "item")
    producible = parseBoolean(jsonElement, "producible", producible)
    consumable = parseBoolean(jsonElement, "consumable", consumable)
    identifiable = parseBoolean(jsonElement, "identifiable", identifiable)
    holdable = parseBoolean(jsonElement, "holdable", holdable)
    wearable = parseBoolean(jsonElement, "wearable", wearable)
    usable = parseBoolean(jsonElement, "usable", usable)
    harmful = parseBoolean(jsonElement, "harmful", harmful)
  }

  override fun toggleEverything() {
    producible = true
    consumable = true
    identifiable = true
    holdable = true
    wearable = true
    usable = true
    harmful = true
  }

  override fun toggleNothing() {
    producible = false
    consumable = false
    identifiable = false
    holdable = false
    wearable = false
    usable = false
    harmful = false
  }
}
