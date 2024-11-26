package net.impleri.playerskills.server.bindings.entity

import com.google.common.annotations.VisibleForTesting
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.HasSource
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.events.EventHandler
import net.impleri.slab.logging.Logger

case class OnHurt(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: EntityEvents = EntityEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
) extends EventHandler {

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
      } yield {
        if (!usable) {
          logger.debug(
            s"${player.handle} cannot attack ${entity.mobTypeName} with ${tool.name}",
          )
        } else {
          logger.trace(
              s"${player.handle} is going to attack ${entity.mobTypeName} with ${tool.name}",
            )
        }

        usable
      }
    }

  upstream.onHurt(handler)
}
