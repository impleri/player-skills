package net.impleri.slab.world

import net.minecraft.world.entity.MobSpawnType

object SpawnType extends Enumeration {
  val NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, BREEDING, MOB_SUMMONED,
    JOCKEY, EVENT, CONVERSION, REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_EGG,
    COMMAND, DISPENSER, PATROL = Value

  def fromVanilla(spawnType: MobSpawnType): Value = {
    spawnType match {
      case MobSpawnType.NATURAL          => NATURAL
      case MobSpawnType.CHUNK_GENERATION => CHUNK_GENERATION
      case MobSpawnType.SPAWNER          => SPAWNER
      case MobSpawnType.STRUCTURE        => STRUCTURE
      case MobSpawnType.BREEDING         => BREEDING
      case MobSpawnType.MOB_SUMMONED     => MOB_SUMMONED
      case MobSpawnType.JOCKEY           => JOCKEY
      case MobSpawnType.EVENT            => EVENT
      case MobSpawnType.CONVERSION       => CONVERSION
      case MobSpawnType.REINFORCEMENT    => REINFORCEMENT
      case MobSpawnType.TRIGGERED        => TRIGGERED
      case MobSpawnType.BUCKET           => BUCKET
      case MobSpawnType.SPAWN_EGG        => SPAWN_EGG
      case MobSpawnType.COMMAND          => COMMAND
      case MobSpawnType.DISPENSER        => DISPENSER
      case MobSpawnType.PATROL           => PATROL
    }
  }
}
