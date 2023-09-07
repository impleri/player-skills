package net.impleri.playerskills.integrations.kubejs.items

import net.impleri.playerskills.integrations.kubejs.api.AbstractRestrictionConditionsBuilder
import net.impleri.playerskills.integrations.kubejs.api.PlayerDataJS
import net.impleri.playerskills.restrictions.items.ItemConditions
import net.impleri.playerskills.restrictions.items.ItemRestriction
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.Item

class ItemConditionsBuilderJS(
  server: Lazy<MinecraftServer>,
) : AbstractRestrictionConditionsBuilder<Item, ItemRestriction>(server), ItemConditions<PlayerDataJS> {
  override var replacement: Item? = null
  override var producible: Boolean? = false
  override var consumable: Boolean? = false
  override var holdable: Boolean? = false
  override var identifiable: Boolean? = false
  override var harmful: Boolean? = false
  override var wearable: Boolean? = false
  override var usable: Boolean? = false
}
