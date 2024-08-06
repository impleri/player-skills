package net.impleri.playerskills.server.bindings.entity

import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.events.EventHandler
import net.impleri.slab.logging.Logger

case class BeforeSpawn(
  itemRestrictionOps: ItemRestrictionOps,
  upstream: EntityEvents = EntityEvents(),
  logger: Logger = PlayerSkillsLogger.ITEMS,
  skipLogger: Logger = PlayerSkillsLogger.SKIPS,
) extends EventHandler {
  private[bindings] val handler: EntityEvents.CanSpawn =
    (entity, levelOpt, positionOpt, spawnTypeOpt, spawnerOpt) => {
      skip
      //      if (!MobRestrictions.canSpawn(livingEntity, levelAccessor, pos, mobSpawnType)) {
      //        PlayerSkillsLogger.MOBS.debug("$mobType cannot spawn at $posString")
      //        EventResult.interruptFalse()
      //      } else {
      //        PlayerSkillsLogger.SKIPS.debug("$mobType is going to spawn at $posString")
      //        EventResult.pass()
      //      }
    }

  upstream.canSpawn(handler)
}
