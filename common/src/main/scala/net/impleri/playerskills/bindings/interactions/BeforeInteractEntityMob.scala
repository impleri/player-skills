package net.impleri.playerskills.bindings.interactions

import net.impleri.playerskills.utils.{EventLogging, PlayerSkillsLogger}
import net.impleri.slab.entity.Hand.Hand
import net.impleri.slab.entity.{Entity, Player}
import net.impleri.slab.events.{EventHandler, InteractionEvents}
import net.impleri.slab.logging.Logger

case class BeforeInteractEntityMob(
//    mobRestrictionOps: MobRestrictionOps,
  upstream: InteractionEvents = InteractionEvents(),
  logger: Logger = PlayerSkillsLogger.MOBS,
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
          canInteract = false // mobRestrictionOps.canInteractWith(player, entity.getType)
        } yield logEvent(player, s"interact with entity $entity using $item")(canInteract)
      }
  }

  upstream.onRightClickEntity(handler)
}
