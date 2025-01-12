package net.impleri.playerskills.client.bindings

import net.impleri.playerskills.client.restrictions.ItemRestrictionOpsClient
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.client.Client
import net.impleri.slab.client.events.TooltipEvents
import net.impleri.slab.logging.Logger

import scala.util.chaining.scalaUtilChainingOps

case class OnTooltipItem(
  client: Client,
  itemRestrictionOpsClient: ItemRestrictionOpsClient,
  logger: Logger = PlayerSkillsLogger.ITEMS,
  tooltipEvents: TooltipEvents = TooltipEvents(),
) extends EventLogging {
  private val handler: TooltipEvents.ON_RENDER = (item, _, _) =>
    itemRestrictionOpsClient.isIdentifiable(item, None)
      .pipe(logEvent(client.getPlayer.get, s"identify $item"))
      .pipe(Option(_))
      .filterNot(identity)
      .map(_ => List(OnTooltipItem.UnknownItem))

  tooltipEvents.onRenderItem(handler)
}

object OnTooltipItem {
  private lazy val UnknownItem = TranslatableText("message.playerskills.unknown_item").red()
}
