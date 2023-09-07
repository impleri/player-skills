package net.impleri.playerskills.restrictions.items

import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.restrictions.AbstractRestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

class ItemRestrictionBuilder : AbstractRestrictionBuilder<Item, ItemRestriction>(
  Registry.ITEM,
  PlayerSkillsLogger.ITEMS,
) {
  override fun <Player> restrictOne(
    targetName: ResourceLocation,
    builder: RestrictionConditionsBuilder<Item, Player, ItemRestriction>,
  ) {
    builder as ItemConditions<Player>

    val item = ItemRestrictions.getValue(targetName)

    if (ItemRestrictions.isDefaultItem(item) && targetName != ItemRestrictions.getName(Items.AIR)) {
      logger.warn("Could not find any item named $targetName")
      return
    }

    val restriction = ItemRestriction(
      item,
      builder.actualCondition,
      null,
      builder.includeDimensions,
      builder.excludeDimensions,
      builder.includeBiomes,
      builder.excludeBiomes,
      builder.producible,
      builder.consumable,
      builder.holdable,
      builder.identifiable,
      builder.harmful,
      builder.wearable,
      builder.usable,
    )

    ItemRestrictions.add(targetName, restriction)
    logRestriction(targetName, restriction)
  }

  override fun getName(target: Item): ResourceLocation {
    return ItemRestrictions.getName(target)
  }

  override fun isTagged(target: Item, tag: TagKey<Item>): Boolean {
    return target.defaultInstance.`is`(tag)
  }

  companion object {
    private val instance = ItemRestrictionBuilder()

    fun <Player> register(name: String, builder: ItemConditions<Player>) {
      instance.create(name, builder)
    }

    fun register() {
      instance.register()
    }
  }
}
