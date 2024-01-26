package net.impleri.playerskills.client.restrictions

import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger

case class ItemRestrictionOpsClient(
  r: RestrictionRegistry = RestrictionRegistry(),
  override val client: Client = Client(),
  l: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends ItemRestrictionOps(r, l) with RestrictionOpsClient {
  def isIdentifiable(item: Item, pos: Option[Position]): Boolean = {
    isIdentifiable(getPlayer, item, pos)
  }

  def isHoldable(item: Item, pos: Option[Position]): Boolean = {
    isHoldable(getPlayer, item, pos)
  }

  def isWearable(item: Item, pos: Option[Position]): Boolean = {
    isWearable(getPlayer, item, pos)
  }

  def isUsable(item: Item, pos: Option[Position]): Boolean = {
    isUsable(getPlayer, item, pos)
  }

  def isHarmful(item: Item, pos: Option[Position]): Boolean = {
    isHarmful(getPlayer, item, pos)
  }
}
