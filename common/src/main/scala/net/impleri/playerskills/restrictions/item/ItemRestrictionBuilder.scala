package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.RestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.item.{Item => McItem}

case class ItemRestrictionBuilder(
  override val registry: Registry[McItem],
  protected val restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[McItem, ItemConditions] {
  override val singleAsString: Boolean = true

  private def restrictItem(item: Item, builder: ItemConditions, targetName: String): Unit = {
    val restriction = ItemRestriction(item, builder)

    restrictionRegistry.add(restriction)
    logRestriction(targetName, restriction)
  }

  override protected def restrictOne(
    targetName: ResourceLocation,
    builder: ItemConditions,
  ): Unit = {
    registry.get(targetName)
      .map(Item(_))
      .foreach(restrictItem(_, builder, targetName.toString))
  }

  override def restrictString(
    targetName: String,
    builder: ItemConditions,
  ): Unit = {
    Item.parse(targetName)
      .foreach(restrictItem(_, builder, targetName))
  }
}
