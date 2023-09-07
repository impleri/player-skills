package net.impleri.playerskills.integrations.kubejs.mobs

import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.integrations.kubejs.api.AbstractRestrictionConditionsBuilder
import net.impleri.playerskills.integrations.kubejs.api.PlayerDataJS
import net.impleri.playerskills.restrictions.mobs.MobConditions
import net.impleri.playerskills.restrictions.mobs.MobRestriction
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import java.util.function.Predicate

class MobConditionsBuilderJS @HideFromJS constructor(
  server: Lazy<MinecraftServer>,
) : AbstractRestrictionConditionsBuilder<EntityType<*>, MobRestriction>(server), MobConditions<PlayerDataJS> {
  @HideFromJS
  override var replacement: EntityType<*>? = null

  @HideFromJS
  override var spawnMode: EntitySpawnMode = EntitySpawnMode.ALLOW_ALWAYS

  @HideFromJS
  override var usable: Boolean? = true

  @HideFromJS
  override var includeSpawners: MutableList<MobSpawnType> = ArrayList()

  @HideFromJS
  override var excludeSpawners: MutableList<MobSpawnType> = ArrayList()

  override fun unless(predicate: Predicate<PlayerDataJS>): MobConditionsBuilderJS {
    super<MobConditions>.unless(predicate)

    return this
  }

  fun spawnable(): MobConditionsBuilderJS {
    spawnable(null)

    return this
  }

  fun unspawnable(): MobConditionsBuilderJS {
    unspawnable(null)

    return this
  }

  @HideFromJS
  override fun fromSpawner(spawner: MobSpawnType): MobConditionsBuilderJS {
    return super.fromSpawner(spawner) as MobConditionsBuilderJS
  }

  @HideFromJS
  override fun notFromSpawner(spawner: MobSpawnType): MobConditionsBuilderJS {
    return super.notFromSpawner(spawner) as MobConditionsBuilderJS
  }
}
