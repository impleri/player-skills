package net.impleri.playerskills.server.bindings

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

case class EntityEvents(
  itemRestrictionOps: ItemRestrictionOps,
  onLivingHurt: Event[EntityEvent.LivingHurt] = EntityEvent.LIVING_HURT,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  skipLogger: PlayerSkillsLogger = PlayerSkillsLogger.SKIPS,
) {
  def registerEvents(): Unit = {
    //    EntityEvent.LIVING_CHECK_SPAWN.register(
    //      EntityEvent.LivingCheckSpawn { livingEntity: LivingEntity, levelAccessor: LevelAccessor, x: Double, y: Double, z: Double, mobSpawnType: MobSpawnType, _: BaseSpawner? ->
    //        onCheckSpawn(
    //          livingEntity,
    //          levelAccessor,
    //          x,
    //          y,
    //          z,
    //          mobSpawnType,
    //        )
    //      },
    //    )

    onLivingHurt.register { (entity: LivingEntity, source: DamageSource, _: Float) =>
      beforePlayerAttack(Entity(entity), Entity(source))
    }
  }

  //  private def onCheckSpawn(
  //    livingEntity: LivingEntity,
  //    levelAccessor: LevelAccessor,
  //    x: Double,
  //    y: Double,
  //    z: Double,
  //    mobSpawnType: MobSpawnType,
  //  ): EventResult = {
  //    val pos = Vec3i(x, y, z)
  //    val posString = pos.toShortString()
  //    val mobType = MobRestrictions.getName(livingEntity.type)
  //
  //    if (!MobRestrictions.canSpawn(livingEntity, levelAccessor, pos, mobSpawnType)) {
  //      PlayerSkillsLogger.MOBS.debug("$mobType cannot spawn at $posString")
  //      return EventResult.interruptFalse()
  //    }
  //
  //    PlayerSkillsLogger.SKIPS.debug("$mobType is going to spawn at $posString")
  //    return EventResult.pass()
  //  }

  private[bindings] def beforePlayerAttack(entity: Entity[_], attacker: Entity[_]): EventResult = {
    if (attacker.isPlayer) {
      val player = attacker.asPlayer
      if (!itemRestrictionOps.isHarmful(player, player.getItemInMainHand)) {
        logger.debug(s"${attacker.name} cannot attack ${entity.mobTypeName} with ${player.getItemInMainHand.name}")
        return EventResult.interruptFalse()
      }

      skipLogger
        .debug(s"${attacker.name} is going to attack ${entity.mobTypeName} with ${player.getItemInMainHand.name}")
    }

    EventResult.pass()
  }
}
