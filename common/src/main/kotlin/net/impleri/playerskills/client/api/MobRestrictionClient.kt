package net.impleri.playerskills.client.api

import net.impleri.playerskills.api.MobRestrictions
import net.impleri.playerskills.client.RestrictionsClient
import net.impleri.playerskills.mobs.MobRestriction
import net.impleri.playerskills.restrictions.Registry
import net.impleri.playerskills.restrictions.RestrictionsApi
import net.minecraft.world.entity.EntityType

class MobRestrictionClient(
  registry: Registry<MobRestriction>,
  serverApi: MobRestrictions,
) : RestrictionsClient<EntityType<*>, MobRestriction, MobRestrictions>(
  registry,
  serverApi,
) {
  internal fun isUsable(entity: EntityType<*>): Boolean {
    return player?.let { serverApi.isUsable(it, entity) } ?: RestrictionsApi.DEFAULT_CAN_RESPONSE
  }

  companion object {
    private val INSTANCE = MobRestrictionClient(MobRestrictions.RestrictionRegistry, MobRestrictions.INSTANCE)

    fun isUsable(entity: EntityType<*>): Boolean {
      return INSTANCE.isUsable(entity)
    }
  }
}
