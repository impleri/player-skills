package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.api.restrictions.RestrictionsOps
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

class ItemRestrictionOps(
  protected val registry: RestrictionRegistry,
  protected val logger: PlayerSkillsLogger,
) extends RestrictionsOps[Item, ItemRestriction] {
  override val restrictionType: RestrictionType = RestrictionType.Item()

  def isIdentifiable(player: Player[_], item: Item, pos: Option[Position] = None): Boolean = {
    canPlayer(player, item, _.identifiable, "identifiable", pos)
  }

  def isHoldable(player: Player[_], item: Item, pos: Option[Position] = None): Boolean = {
    canPlayer(player, item, _.holdable, "holdable", pos)
  }

  def isWearable(player: Player[_], item: Item, pos: Option[Position] = None): Boolean = {
    canPlayer(player, item, _.wearable, "wearable", pos)
  }

  def isUsable(player: Player[_], item: Item, pos: Option[Position] = None): Boolean = {
    canPlayer(player, item, _.usable, "usable", pos)
  }

  def isHarmful(player: Player[_], item: Item, pos: Option[Position] = None): Boolean = {
    canPlayer(player, item, _.harmful, "harmful", pos)
  }
}

object ItemRestrictionOps {
  def apply(
    registry: RestrictionRegistry = RestrictionRegistry(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  ): ItemRestrictionOps = {
    new ItemRestrictionOps(registry, logger)
  }
}
