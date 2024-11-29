package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
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
) extends EventHandler with EventLogging {
  private[bindings] def handler: InteractionEvents.OnClickEntity = {
    (
      player: Player,
      entityOpt: Option[Entity[_]],
      hand: Hand,
    ) =>
      failOn {
        for {
          entity <- entityOpt
          item <- player.getItemInHand(hand).filterNot(_.isDefault)
          usable = itemRestrictionOps.isUsable(player, item, None)
        } yield logEvent(player, s"interact with entity ${entity.mobTypeName} using ${item.name}")(usable)
          //    val mobType = MobRestrictions.getName(entity.type)
          //    if (!MobRestrictions.canInteractWith(entity.type, player)) {
          //      PlayerSkillsLogger.MOBS.debug("${player.handle} cannot interact with entity $mobType")
          //      return EventResult.interruptFalse()
          //    }
      }
  }

  upstream.onRightClickEntity(handler)
}
