package net.impleri.slab.events

import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.Event
import dev.architectury.event.EventResult
import net.impleri.slab.entity.Animal
import net.impleri.slab.entity.DamageType
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Player
import net.impleri.slab.world.Coordinates
import net.impleri.slab.world.Level
import net.impleri.slab.world.Position
import net.impleri.slab.world.Spawner
import net.impleri.slab.world.SpawnType
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.animal.{Animal => McAnimal}
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.LevelAccessor

case class EntityEvents(
  private val onDeathEvent: Event[EntityEvent.LivingDeath] =
    EntityEvent.LIVING_DEATH,
  private val onHurtEvent: Event[EntityEvent.LivingHurt] =
    EntityEvent.LIVING_HURT,
  private val canSpawnEvent: Event[EntityEvent.LivingCheckSpawn] =
    EntityEvent.LIVING_CHECK_SPAWN,
  private val onSpawnEvent: Event[EntityEvent.Add] = EntityEvent.ADD,
  private val onEnterChunkEvent: Event[EntityEvent.EnterSection] =
    EntityEvent.ENTER_SECTION,
  private val onTameEvent: Event[EntityEvent.AnimalTame] =
    EntityEvent.ANIMAL_TAME,
) extends ResultHandler {
  def onDeath(handler: EntityEvents.OnDeath): Unit =
    onDeathEvent.register { (rawEntity: LivingEntity, rawSource: DamageSource) =>
      ensureResult {
        for {
          entity <- Option(rawEntity).map(Entity(_))
          damageType = Option(rawSource).map(DamageType(_))
        } yield handler(entity, damageType)
      }
    }

  def onHurt(handler: EntityEvents.OnHurt): Unit =
    onHurtEvent.register {
      (rawEntity: LivingEntity, rawSource: DamageSource, damage: Float) =>
        ensureResult {
          for {
            entity <- Option(rawEntity).map(Entity(_))
            damageType = Option(rawSource).map(DamageType(_))
          } yield handler(entity, damageType, damage)
        }
    }

  def shouldSpawn(handler: EntityEvents.CanSpawn): Unit =
    canSpawnEvent.register {
        (
          rawEntity: LivingEntity,
          rawWorld: LevelAccessor,
          x: Double,
          y: Double,
          z: Double,
          rawSpawnType: MobSpawnType,
          rawSpawner: BaseSpawner,
        ) =>
          ensureResult {
            for {
              entity <- Option(rawEntity).map(Entity(_))
              level = Option(rawWorld).map(Level(_))
              position = Coordinates(x, y, z).map(_.toPosition)
              spawnType = Option(rawSpawnType).map(SpawnType.fromVanilla)
              spawner = Option(rawSpawner).map(Spawner(_))
            } yield handler(entity, level, position, spawnType, spawner)
          }
      }

  def onSpawn(handler: EntityEvents.OnSpawn): Unit =
    onSpawnEvent.register { (rawEntity: McEntity, rawWorld: McLevel) =>
      ensureResult {
        for {
          entity <- Option(rawEntity).map(Entity(_))
          level = Option(rawWorld).map(Level(_))
        } yield handler(entity, level)
      }
    }

  def onEnterChunk(handler: EntityEvents.OnEnterChunk): Unit =
    onEnterChunkEvent.register {
      (rawEntity: McEntity, x: Int, y: Int, z: Int, px: Int, py: Int, pz: Int) =>
        for {
          entity <- Option(rawEntity).map(Entity(_))
          position = Coordinates(x, y, z)
          previous = Coordinates(px, py, pz)
        } yield handler(entity, position, previous)
    }

  def onTame(handler: EntityEvents.OnTame): Unit =
    onTameEvent.register { (rawAnimal: McAnimal, rawPlayer: McPlayer) =>
      ensureResult {
        for {
          player <- Option(rawPlayer).map(Player(_))
          animal = Option(rawAnimal).map(Animal(_))
        } yield handler(player, animal)
      }
    }
}

object EntityEvents {
  type OnDeath = (Entity.Any, Option[DamageType]) => EventResult
  type OnHurt = (Entity.Any, Option[DamageType], Float) => EventResult
  type CanSpawn = (
    Entity.Any,
    Option[Level.Any],
    Option[Position],
    Option[SpawnType.Value],
    Option[Spawner.Any],
  ) => EventResult
  type OnSpawn = (Entity.Any, Option[Level.Any]) => EventResult
  type OnEnterChunk =
    (Entity.Any, Option[Coordinates], Option[Coordinates]) => Unit
  type OnTame = (Player, Option[Animal.Any]) => EventResult
}
