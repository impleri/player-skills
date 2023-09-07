package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.RestrictionDataParser
import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import java.util.function.Predicate

abstract class AbstractRestrictionConditionBuilder<Target, Restriction : AbstractRestriction<Target>>(override val name: ResourceLocation? = null) :
  RestrictionConditionsBuilder<Target, Player, Restriction>, RestrictionDataParser {
  override val server: Lazy<MinecraftServer> = EventHandlers.server
  override var target: String? = null
  override val includeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val excludeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val includeDimensions: MutableList<ResourceLocation> = ArrayList()
  override val excludeDimensions: MutableList<ResourceLocation> = ArrayList()
  override var rawCondition = Predicate { _: Player -> true }
  override val actualCondition: (Player) -> Boolean
    get() = {
      rawCondition.test(it)
    }
  val targetName: String
    get() = target ?: name?.toString() ?: throw NullPointerException("Restrictions must provide a target name")

  fun parse(
    jsonElement: JsonObject,
  ) {
    parseDimensions(jsonElement, { inDimension(it) }, { notInDimension(it) })
    parseBiomes(jsonElement, { inBiome(it) }, { notInBiome(it) })
    parseCondition(jsonElement)
    parseRestriction(jsonElement)
    parseEverything(jsonElement)
    parseNothing(jsonElement)
  }

  protected fun parseTarget(raw: JsonObject, element: String = "target") {
    target =
      parseValue(raw, element, { it.asString }) ?: throw NullPointerException("Restrictions must provide a $element")
  }

  private fun parseCondition(raw: JsonObject) {
    val conditions = parseIf(raw)
    val unless = parseUnless(raw)

    rawCondition = Predicate { player: Player ->
      (conditions.isEmpty() || conditions.all { it(player) }) &&
        (unless.isEmpty() || unless.none { it(player) })
    }
  }

  private fun parseEverything(raw: JsonObject) {
    parseBoolean(raw, "everything")?.let { toggleEverything() }
  }

  private fun parseNothing(raw: JsonObject) {
    parseBoolean(raw, "nothing")?.let { toggleNothing() }
  }

  abstract fun parseRestriction(jsonElement: JsonObject)

  abstract fun toggleEverything()

  abstract fun toggleNothing()
}
