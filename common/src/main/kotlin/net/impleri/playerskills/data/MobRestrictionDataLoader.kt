package net.impleri.playerskills.data

import com.google.gson.JsonObject
import net.impleri.playerskills.api.MobRestrictionBuilder
import net.impleri.playerskills.data.conditions.MobRestrictionConditionBuilder
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.mobs.MobRestriction
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
    val type = parseValue(jsonElement, "entity", { it.asString })
      ?: throw NullPointerException("Restrictions must target an entity")

    val builder = MobRestrictionConditionBuilder()
    builder.parse(jsonElement)

    MobRestrictionBuilder.register(type, builder)
  }
}
