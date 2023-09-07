package net.impleri.playerskills.integrations.crafttweaker.restrictions

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.api.EntitySpawnMode
import net.impleri.playerskills.restrictions.mobs.MobConditions
import net.impleri.playerskills.restrictions.mobs.MobRestriction
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.player.Player
import org.openzen.zencode.java.ZenCodeType
import java.util.function.Predicate

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.MobConditionsBuilder")
class MobConditionsBuilder(
  private val onSave: (MobConditionsBuilder) -> Unit,
) : AbstractRestrictionConditionsBuilder<EntityType<*>, MobRestriction>(), MobConditions<Player> {
  override var replacement: EntityType<*>? = null
  override var spawnMode: EntitySpawnMode = EntitySpawnMode.ALLOW_ALWAYS
  override var usable: Boolean? = true
  override var includeSpawners: MutableList<MobSpawnType> = ArrayList()
  override var excludeSpawners: MutableList<MobSpawnType> = ArrayList()

  @ZenCodeType.Method
  fun save() {
    onSave(this)
  }

  @ZenCodeType.Method
  override fun condition(predicate: Predicate<Player>): MobConditionsBuilder {
    super<MobConditions>.condition(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun unless(predicate: Predicate<Player>): MobConditionsBuilder {
    super<MobConditions>.unless(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun always(): MobConditionsBuilder {
    super.always()

    return this
  }

  @ZenCodeType.Method
  fun spawnable(): MobConditionsBuilder {
    return spawnable(null)
  }

  @ZenCodeType.Method
  override fun spawnable(requireAll: Boolean?): MobConditionsBuilder {
    super.spawnable(requireAll)

    return this
  }

  @ZenCodeType.Method
  fun unspawnable(): MobConditionsBuilder {
    return unspawnable(null)
  }

  @ZenCodeType.Method
  override fun unspawnable(requireAll: Boolean?): MobConditionsBuilder {
    super.unspawnable(requireAll)

    return this
  }

  @ZenCodeType.Method
  override fun fromSpawner(spawner: String): MobConditionsBuilder {
    super.fromSpawner(spawner)

    return this
  }

  @ZenCodeType.Method
  override fun notFromSpawner(spawner: String): MobConditionsBuilder {
    super.notFromSpawner(spawner)

    return this
  }

  @ZenCodeType.Method
  override fun usable(): MobConditionsBuilder {
    super.usable()

    return this
  }

  @ZenCodeType.Method
  override fun unusable(): MobConditionsBuilder {
    super.unusable()

    return this
  }

  @ZenCodeType.Method
  override fun nothing(): MobConditionsBuilder {
    super.nothing()

    return this
  }

  @ZenCodeType.Method
  override fun everything(): MobConditionsBuilder {
    super.everything()

    return this
  }
}
