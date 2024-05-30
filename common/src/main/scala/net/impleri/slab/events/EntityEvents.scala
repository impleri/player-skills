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
  private val onDeathEvent: Event[EntityEvent.LivingDeath] = EntityEvent.LIVING_DEATH,
  private val onHurtEvent: Event[EntityEvent.LivingHurt] = EntityEvent.LIVING_HURT,
  private val canSpawnEvent: Event[EntityEvent.LivingCheckSpawn] = EntityEvent.LIVING_CHECK_SPAWN,
  private val onSpawnEvent: Event[EntityEvent.Add] = EntityEvent.ADD,
  private val onEnterChunkEvent: Event[EntityEvent.EnterSection] = EntityEvent.ENTER_SECTION,
  private val onTameEvent: Event[EntityEvent.AnimalTame] = EntityEvent.ANIMAL_TAME,
) {
  def onDeath(f: EntityEvents.OnDeath): Unit = {
    onDeathEvent.register { (entity: LivingEntity, source: DamageSource) =>
      Option(entity)
        .map(Entity(_))
        .fold(EventResult.pass)(
          f(
            _,
            Option(source).map(DamageType(_)),
          ),
        )
    }
  }

  def onHurt(f: EntityEvents.OnHurt): Unit = {
    onHurtEvent.register { (entity: LivingEntity, source: DamageSource, damage: Float) =>
      Option(entity)
        .map(Entity(_))
        .fold(EventResult.pass)(
          f(
            _,
            Option(source).map(DamageType(_)),
            damage,
          ),
        )
    }
  }

  def canSpawn(f: EntityEvents.CanSpawn): Unit = {
    canSpawnEvent
      .register { (entity: LivingEntity, world: LevelAccessor, x: Double, y: Double, z: Double, spawnType: MobSpawnType, spawner: BaseSpawner) =>
        Option(entity)
          .map(Entity(_))
          .fold(EventResult.pass)(
            f(
              _,
              Option(world).map(Level(_)),
              Coordinates(x, y, z).map(_.toPosition),
              Option(spawnType).map(SpawnType.fromVanilla),
              Option(spawner).map(Spawner(_)),
            ),
          )
      }
  }

  def onSpawn(f: EntityEvents.OnSpawn): Unit = {
    onSpawnEvent
      .register { (entity: McEntity, world: McLevel) =>
        Option(entity)
          .map(Entity(_))
          .fold(EventResult.pass)(
            f(
              _,
              Option(world).map(Level(_)),
            ),
          )
      }
  }

  def onEnterChunk(f: EntityEvents.OnEnterChunk): Unit = {
    onEnterChunkEvent.register { (entity: McEntity, x: Int, y: Int, z: Int, px: Int, py: Int, pz: Int) =>
      Option(entity)
        .map(Entity(_))
        .foreach(
          f(
            _,
            Coordinates(x, y, z),
            Coordinates(px, py, pz),
          ),
        )

    }
  }

  def onTame(f: EntityEvents.OnTame): Unit = {
    onTameEvent.register { (animal: McAnimal, player: McPlayer) =>
      Option(player)
        .map(Player(_))
        .fold(EventResult.pass)(
          f(
            _,
            Option(animal).map(Animal(_)),
          ),
        )

    }
  }
}

object EntityEvents {
  type OnDeath = (Entity.Any, Option[DamageType]) => EventResult
  type OnHurt = (Entity.Any, Option[DamageType], Float) => EventResult
  type CanSpawn = (Entity.Any, Option[Level.Any], Option[Position], Option[SpawnType.Value], Option[Spawner.Any]) => EventResult
  type OnSpawn = (Entity.Any, Option[Level.Any]) => EventResult
  type OnEnterChunk = (Entity.Any, Option[Coordinates], Option[Coordinates]) => Unit
  type OnTame = (Player.Any, Option[Animal.Any]) => EventResult
}
