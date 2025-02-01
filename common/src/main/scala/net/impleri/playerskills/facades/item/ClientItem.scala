package net.impleri.playerskills.facades.item

import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.chat.Message
import net.impleri.slab.item.Item

object ClientItem {

  def getRestrictedName(item: Item.Vanilla): Option[Message.Any] = {
    Option(item)
      .map(Item.fromVanilla)
      .map(PlayerSkillsClient.STATE.ITEM_RESTRICTIONS.isIdentifiable(_, None))
      .filterNot(identity)
      .map(_ => ItemRestrictionOps.UnknownItem)
  }

  def handleGetDescriptionId(item: Item): Option[String] =
    Option(PlayerSkillsClient.STATE.ITEM_RESTRICTIONS.isIdentifiable(item, None))
      .filterNot(identity)
      .map(_ => ItemRestrictionOps.UnknownItemId)
}
