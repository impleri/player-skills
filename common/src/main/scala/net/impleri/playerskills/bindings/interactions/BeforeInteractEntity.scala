package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.events.EventHandler
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.logging.Logger

case class BeforeInteractEntity(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends EventHandler {
  private[bindings] def handler: InteractionEvents.OnClickEntity = {
    (
      player: Player[_],
      entityOpt: Option[Entity[_]],
      hand: Hand,
    ) =>
      {
        val result = for {
          entity <- entityOpt
          item <- player.getItemInHand(hand).filterNot(_.isDefault)
          usable = itemRestrictionOps.isUsable(player, item, None)
        } yield {
          //    val mobType = MobRestrictions.getName(entity.type)
          //    if (!MobRestrictions.canInteractWith(entity.type, player)) {
          //      PlayerSkillsLogger.MOBS.debug("${player.handle} cannot interact with entity $mobType")
          //      return EventResult.interruptFalse()
          //    }
          if (!usable) {
            logger.debug(
              s"${player.handle} cannot interact with entity ${entity.mobTypeName} using ${item.name}",
            )
          } else {
            skipLogger
              .debug(
                s"${player.handle} is going to interact with entity ${entity.mobTypeName} using ${item.name}",
              )
          }

          usable
        }

        failOn(result)
      }
  }

  upstream.onRightClickEntity(handler)
}
