package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.EventHandlers
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import java.util.function.Predicate

abstract class AbstractRestrictionConditionBuilder<Target, Restriction : AbstractRestriction<Target>> :
  RestrictionConditionsBuilder<Target, Player, Restriction>, RestrictionDataParser {
  override val server: Lazy<MinecraftServer> = EventHandlers.server
  override val includeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val excludeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val includeDimensions: MutableList<ResourceLocation> = ArrayList()
  override val excludeDimensions: MutableList<ResourceLocation> = ArrayList()
  override var rawCondition = Predicate { _: Player -> true }
  override val actualCondition: (Player) -> Boolean
    get() = {
      rawCondition.test(it)
    }

  fun parse(
    jsonElement: JsonObject,
  ) {
    parseDimensions(jsonElement, { inDimension(it) }, { notInDimension(it) })
    parseBiomes(jsonElement, { inBiome(it) }, { notInBiome(it) })
    parseCondition(jsonElement)
    parseRestriction(jsonElement)
  }

  private fun parseCondition(raw: JsonObject) {
    val conditions = parseIf(raw)
    val unless = parseUnless(raw)

    rawCondition = Predicate { player: Player ->
      (conditions.isEmpty() || conditions.all { it(player) }) &&
        (unless.isEmpty() || unless.none { it(player) })
    }
  }

  abstract fun parseRestriction(jsonElement: JsonObject)
}
