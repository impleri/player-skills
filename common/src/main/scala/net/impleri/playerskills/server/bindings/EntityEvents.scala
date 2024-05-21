package net.impleri.playerskills.server.bindings

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.Event
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.EventUtils
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

case class EntityEvents(
  itemRestrictionOps: ItemRestrictionOps,
  onLivingHurt: Event[EntityEvent.LivingHurt] = EntityEvent.LIVING_HURT,
  logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  skipLogger: PlayerSkillsLogger = PlayerSkillsLogger.SKIPS,
) extends EventUtils {
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
      beforePlayerAttack(
        Option(entity).map(Entity(_)),
        Option(source).flatMap(s => Option(s.getEntity)).map(Entity(_)),
      )
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

  private[bindings] def beforePlayerAttack(
    entityOpt: Option[Entity[_]],
    attackerOpt: Option[Entity[_]],
  ): EventResult = {
    val result = for {
      entity <- entityOpt
      player <- attackerOpt.filter(_.isPlayer).map(_.asPlayer)
      tool <- player.getItemInMainHand
      usable = itemRestrictionOps.isHarmful(player, tool)
    } yield {
      if (!usable) {
        logger.debug(s"${player.name} cannot attack ${entity.mobTypeName} with ${tool.name}")
      } else {

        skipLogger
          .debug(s"${player.name} is going to attack ${entity.mobTypeName} with ${tool.name}")
      }

      usable
    }

    failOn(result)
  }
}
