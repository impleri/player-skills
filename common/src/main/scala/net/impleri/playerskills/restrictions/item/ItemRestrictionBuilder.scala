package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.restrictions.RestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class ItemRestrictionBuilder(
  override val registry: Option[Registry.ITEM],
  protected val restrictionRegistry: RestrictionRegistry =
    RestrictionRegistry(),
  override val logger: Logger = PlayerSkillsLogger.ITEMS,
) extends RestrictionBuilder[Item, Item.Vanilla, ItemConditions] {
  override val singleAsString: Boolean = true

  private def restrictItem(
    item: Item,
    builder: ItemConditions,
    targetName: String,
  ): Unit =
    ItemRestriction(item, builder)
      .tap(restrictionRegistry.add)
      .tap(logRestriction(item.asString, _))

  override protected[item] def restrictOne(
    targetName: ResourceLocation,
    builder: ItemConditions,
  ): Unit =
    for {
      reg <- registry
      target <- reg.find(targetName)
    } yield restrictItem(target, builder, targetName.toString)

  override def restrictString(
    targetName: String,
    builder: ItemConditions,
  ): Unit =
    for {
      target <- Item.parse(targetName)
    } yield restrictItem(target, builder, targetName)
}
