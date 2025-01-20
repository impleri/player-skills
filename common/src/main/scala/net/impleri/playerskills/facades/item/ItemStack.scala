package net.impleri.playerskills.facades.item

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.slab.chat.Message
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item

import scala.jdk.CollectionConverters._

object ItemStack {
  def handleGetTooltipLines(playerOpt: Option[Player], item: Item): Option[java.util.List[Message.VanillaBase]] = {
    for {
      player <- playerOpt
      isIdentifiable = PlayerSkills.STATE.ITEM_RESTRICTIONS.isIdentifiable(player, item)
      _ <- Option(isIdentifiable).filterNot(identity)
    } yield List(ItemRestrictionOps.UnknownItem.output).asJava
  }
}
