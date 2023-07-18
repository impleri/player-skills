package net.impleri.playerskills.mobs

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.LifecycleEvent
import net.impleri.playerskills.api.MobRestrictionBuilder
import net.impleri.playerskills.api.MobRestrictions
import net.minecraft.core.Vec3i
import net.minecraft.server.MinecraftServer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.LevelAccessor

internal class MobEventHandlers {
  private var serverInstance: MinecraftServer? = null

  internal val server: MinecraftServer by lazy {
    serverInstance ?: throw RuntimeException("Unable to access the server before it is available")
  }

  fun registerEvents() {
    // Architectury Events
    LifecycleEvent.SERVER_STARTED.register(LifecycleEvent.ServerState { onServerStart() })

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

    InteractionEvent.INTERACT_ENTITY.register(
      InteractionEvent.InteractEntity { player: Player, entity: Entity, _: InteractionHand ->
        onInteract(
          player,
          entity,
        )
      },
    )
  }

  private fun onServerStart() {
    MobRestrictionBuilder.register()
  }

  private fun onInteract(player: Player, entity: Entity): EventResult {
    if (MobRestrictions.canInteractWith(entity.type, player)) {
      return EventResult.pass()
    }
    MobSkills.LOGGER.debug("Preventing ${player.name} from interacting with ${MobRestrictions.getName(entity.type)}")
    return EventResult.interruptFalse()
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
    if (MobRestrictions.canSpawn(livingEntity, levelAccessor, pos, mobSpawnType)) {
      return EventResult.pass()
    }
    MobSkills.LOGGER.debug("Preventing ${MobRestrictions.getName(livingEntity.type)} from spawning at ${pos.toShortString()}")
    return EventResult.interruptFalse()
  }

  companion object {
    private val INSTANCE = MobEventHandlers()

    fun init() {
      INSTANCE.registerEvents()
    }
  }
}
