package net.impleri.playerskills.data

import com.google.gson.JsonObject
import net.impleri.playerskills.data.conditions.MobRestrictionConditionBuilder
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.mobs.MobRestriction
import net.impleri.playerskills.restrictions.mobs.MobRestrictionBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType

class MobRestrictionDataLoader :
  AbstractRestrictionDataLoader<EntityType<*>, MobRestriction>(
    "mob_restrictions",
  ),
  RestrictionDataParser {
  override fun parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ) {
    val builder = MobRestrictionConditionBuilder(name)
    builder.parse(jsonElement)

    MobRestrictionBuilder.register(builder.targetName, builder)
  }
}
