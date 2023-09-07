package net.impleri.playerskills.events.handlers

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.api.MobRestrictions
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.Vec3i
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.LevelAccessor

class EntityEvents {
  fun registerEventHandlers() {
    EntityEvent.LIVING_CHECK_SPAWN.register(
      EntityEvent.LivingCheckSpawn { livingEntity: LivingEntity, levelAccessor: LevelAccessor, x: Double, y: Double, z: Double, mobSpawnType: MobSpawnType, _: BaseSpawner? ->
        onCheckSpawn(
          livingEntity,
          levelAccessor,
          x,
          y,
          z,
          mobSpawnType,
        )
      },
    )

    EntityEvent.LIVING_HURT.register(
      EntityEvent.LivingHurt { entity: LivingEntity, source: DamageSource, _: Float ->
        beforePlayerAttack(
          entity,
          source,
        )
      },
    )
  }

  private fun onCheckSpawn(
    livingEntity: LivingEntity,
    levelAccessor: LevelAccessor,
    x: Double,
    y: Double,
    z: Double,
    mobSpawnType: MobSpawnType,
  ): EventResult {
    val pos = Vec3i(x, y, z)
    val posString = pos.toShortString()
    val mobType = MobRestrictions.getName(livingEntity.type)

    if (!MobRestrictions.canSpawn(livingEntity, levelAccessor, pos, mobSpawnType)) {
      PlayerSkillsLogger.MOBS.debug("$mobType cannot spawn at $posString")
      return EventResult.interruptFalse()
    }

    PlayerSkillsLogger.SKIPS.debug("$mobType is going to spawn at $posString")
    return EventResult.pass()
  }

  private fun beforePlayerAttack(entity: LivingEntity, source: DamageSource): EventResult {
    val attacker = source.entity
    if (attacker is Player) {
      val mobType = MobRestrictions.getName(entity.type)

      val weapon = ItemRestrictions.getValue(attacker.mainHandItem)
      val weaponName = ItemRestrictions.getName(weapon)

      if (!ItemRestrictions.canAttackWith(attacker, weapon, null)) {
        PlayerSkillsLogger.ITEMS.debug("${attacker.name.string} cannot attack $mobType with $weaponName")
        return EventResult.interruptFalse()
      }

      PlayerSkillsLogger.SKIPS.debug("${attacker.name.string} is going to attack $mobType with $weaponName")
    }

    return EventResult.pass()
  }
}
