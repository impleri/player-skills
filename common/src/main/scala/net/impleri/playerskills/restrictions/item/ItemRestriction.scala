package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item

case class ItemRestriction(
  target: Item,
  condition: Player[_] => Boolean = Restriction.DEFAULT_CONDITION,
  replacement: Option[Item] = None,
  includeDimensions: Seq[String] = List.empty,
  excludeDimensions: Seq[String] = List.empty,
  includeBiomes: Seq[String] = List.empty,
  excludeBiomes: Seq[String] = List.empty,
  holdable: Boolean = Restriction.DEFAULT_RESPONSE,
  wearable: Boolean = Restriction.DEFAULT_RESPONSE,
  harmful: Boolean = Restriction.DEFAULT_RESPONSE,
  usable: Boolean = Restriction.DEFAULT_RESPONSE,
  identifiable: Boolean = Restriction.DEFAULT_RESPONSE,
) extends Restriction[Item, Item.Vanilla] {
  override val restrictionType: RestrictionType = RestrictionType.Item()
}

object ItemRestriction {
  def apply(
    target: Item,
    builder: ItemConditions,
  ): ItemRestriction = {
    new ItemRestriction(
      target,
      builder.condition,
      None,
      builder.includeDimensions,
      builder.excludeDimensions,
      builder.includeBiomes,
      builder.excludeBiomes,
      builder.isHoldable.getOrElse(Restriction.DEFAULT_RESPONSE),
      builder.isWearable.getOrElse(Restriction.DEFAULT_RESPONSE),
      builder.isHarmful.getOrElse(Restriction.DEFAULT_RESPONSE),
      builder.isUsable.getOrElse(Restriction.DEFAULT_RESPONSE),
      builder.isIdentifiable.getOrElse(Restriction.DEFAULT_RESPONSE),
    )
  }
}
