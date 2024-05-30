package net.impleri.playerskills.bindings

import net.impleri.playerskills.bindings.interactions.BeforeInteractEntity
import net.impleri.playerskills.bindings.interactions.BeforeUseItem
import net.impleri.playerskills.bindings.interactions.BeforeUseItemBlock
import net.impleri.playerskills.bindings.player.BeforePlayerPickup
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.events.InteractionEvents
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.logging.Logger

case class EventBindings(
  itemRestrictionOps: ItemRestrictionOps,
  onSetup: () => Unit = () => {},
  commonLifecycle: CommonLifecycleEvents = CommonLifecycleEvents(),
  interaction: InteractionEvents = InteractionEvents(),
  player: PlayerEvents = PlayerEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) {
  def registerEvents(): Unit = {
    commonLifecycle.onSetup(onSetup)
    BeforeUseItem(itemRestrictionOps, interaction, logger, skipLogger)
    BeforeUseItemBlock(itemRestrictionOps, interaction, logger, skipLogger)
    BeforeInteractEntity(itemRestrictionOps, interaction, logger, skipLogger)
    BeforePlayerPickup(itemRestrictionOps, player, logger, skipLogger)
  }
}
