package net.impleri.playerskills.server.bindings.entity

import com.google.common.annotations.VisibleForTesting
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.entity.HasSource
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.events.EventHandler
import net.impleri.slab.logging.Logger

case class OnHurt(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: EntityEvents = EntityEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends EventHandler with EventLogging {

  @VisibleForTesting
  private[bindings] val handler: EntityEvents.OnHurt = (entity, source, _) =>
    failOn {
      for {
        damageType <- source.filter(_.isInstanceOf[HasSource])
        damageSource <- damageType
          .asInstanceOf[HasSource]
          .source
          .filter(_.isPlayer)
        player = damageSource.asPlayer
        tool <- player.getItemInMainHand
        usable = itemRestrictionOps.isHarmful(player, tool)
      } yield logEvent(player, s"attack $entity with $tool")(usable)
    }

  upstream.onHurt(handler)
}
