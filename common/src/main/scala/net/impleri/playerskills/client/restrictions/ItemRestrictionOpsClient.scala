package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class ItemRestrictionOpsClient(
  r: RestrictionRegistry = RestrictionRegistry(),
  protected val client: Client = Client(),
  l: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends ItemRestrictionOps(r, l) with RestrictionOpsClient {
  def isIdentifiable(item: Item, pos: Option[Position]): Boolean = {
    maybeCan(isIdentifiable(_, item, pos))
  }

  def isHoldable(item: Item, pos: Option[Position]): Boolean = {
    maybeCan(isHoldable(_, item, pos))
  }

  def isWearable(item: Item, pos: Option[Position]): Boolean = {
    maybeCan(isWearable(_, item, pos))
  }

  def isUsable(item: Item, pos: Option[Position]): Boolean = {
    maybeCan(isUsable(_, item, pos))
  }

  def isHarmful(item: Item, pos: Option[Position]): Boolean = {
    maybeCan(isHarmful(_, item, pos))
  }
}
