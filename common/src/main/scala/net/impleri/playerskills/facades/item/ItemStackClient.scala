package net.impleri.playerskills.facades.item

import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.chat.Message
import net.impleri.slab.item.Item

object ItemStackClient {
  def handleGetHoverName(item: Item): Option[Message.Any] =
    Option(PlayerSkillsClient.STATE.ITEM_RESTRICTIONS.isIdentifiable(item, None))
      .filterNot(identity)
      .map(_ => ItemRestrictionOps.UnknownItem)

  def handleGetDescriptionId(item: Item): Option[String] =
    Option(PlayerSkillsClient.STATE.ITEM_RESTRICTIONS.isIdentifiable(item, None))
      .filterNot(identity)
      .map(_ => ItemRestrictionOps.UnknownItemId)
}
