package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.RestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{Item => McItem}

case class ItemRestrictionBuilder(
  override val registry: Registry[McItem],
  protected val restrictionRegistry: RestrictionRegistry = RestrictionRegistry(),
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[McItem, ItemConditions] {

  override protected def restrictOne(
    targetName: ResourceLocation,
    builder: ItemConditions,
  ): Unit = {
    val item = registry.get(targetName).map(Item(_))

    if (item.fold(true)(_.isEmpty)) {
      logger.warn(s"Could not find any item named $targetName")
      return
    }

    val restriction = ItemRestriction(item.get, builder)

    restrictionRegistry.add(restriction)
    logRestriction(targetName, restriction)
  }
}
