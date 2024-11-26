package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.world.Position

class ItemRestrictionOps(
  protected val registry: RestrictionRegistry,
  protected val logger: Logger,
) extends RestrictionsOps[Item, Item.Vanilla, ItemRestriction] {
  override val restrictionType: RestrictionType = RestrictionType.Item

  def isIdentifiable(
    player: Player,
    item: Item,
    pos: Option[Position] = None,
  ): Boolean =
    canPlayer(player, item, _.identifiable, "identifiable", pos)

  def isHoldable(
    player: Player,
    item: Item,
    pos: Option[Position] = None,
  ): Boolean =
    canPlayer(player, item, _.holdable, "holdable", pos)

  def isWearable(
    player: Player,
    item: Item,
    pos: Option[Position] = None,
  ): Boolean =
    canPlayer(player, item, _.wearable, "wearable", pos)

  def isUsable(
    player: Player,
    item: Item,
    pos: Option[Position] = None,
  ): Boolean =
    canPlayer(player, item, _.usable, "usable", pos)

  def isHarmful(
    player: Player,
    item: Item,
    pos: Option[Position] = None,
  ): Boolean =
    canPlayer(player, item, _.harmful, "harmful", pos)
}

object ItemRestrictionOps {
  def apply(
    registry: RestrictionRegistry = RestrictionRegistry(),
    logger: Logger = PlayerSkillsLogger.RESTRICTIONS,
  ): ItemRestrictionOps =
    new ItemRestrictionOps(registry, logger)
}
