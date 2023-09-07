package net.impleri.playerskills.data

import com.google.gson.JsonObject
import net.impleri.playerskills.data.conditions.ItemRestrictionConditionBuilder
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.items.ItemRestriction
import net.impleri.playerskills.restrictions.items.ItemRestrictionBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

class ItemRestrictionDataLoader :
  AbstractRestrictionDataLoader<Item, ItemRestriction>(
    "item_restrictions",
  ),
  RestrictionDataParser {
  override fun parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ) {
    val builder = ItemRestrictionConditionBuilder(name)
    builder.parse(jsonElement)

    ItemRestrictionBuilder.register(builder.targetName, builder)
  }
}
