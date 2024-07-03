package net.impleri.playerskills.server.bindings.entity

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
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends EventHandler {
  private[bindings] val handler: EntityEvents.OnHurt = (entity, source, _) => {
    val playerOpt = source match {
      case Some(p: HasSource) if p.isPlayer => p.source.map(_.asPlayer)
      case _ => None
    }
    val result = for {
      player <- playerOpt
      tool <- player.getItemInMainHand
      usable = itemRestrictionOps.isHarmful(player, tool)
    } yield {
      if (!usable) {
        logger.debug(s"${player.handle} cannot attack ${entity.mobTypeName} with ${tool.name}")
      } else {

        skipLogger
          .debug(s"${player.handle} is going to attack ${entity.mobTypeName} with ${tool.name}")
      }

      usable
    }

    failOn(result)
  }

  upstream.onHurt(handler)
}
