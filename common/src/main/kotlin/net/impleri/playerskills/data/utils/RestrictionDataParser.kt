package net.impleri.playerskills.data.utils

import com.google.gson.JsonObject
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as PlayerApi

interface RestrictionDataParser : JsonDataParser {
  fun parseDimensions(
    raw: JsonObject,
    onInclude: (value: String) -> Unit,
    onExclude: (value: String) -> Unit,
  ) {
    parseFacet(raw, "dimensions", onInclude, onExclude)
  }

  fun parseBiomes(raw: JsonObject, onInclude: (value: String) -> Unit, onExclude: (value: String) -> Unit) {
    parseFacet(raw, "biomes", onInclude, onExclude)
  }

  fun parseIf(raw: JsonObject): List<(Player) -> Boolean> {
    return parseObjectOrArray(raw, "if").map { parseCondition(it.asJsonObject) }
  }

  fun parseUnless(raw: JsonObject): List<(Player) -> Boolean> {
    return parseObjectOrArray(raw, "unless").map { parseCondition(it.asJsonObject, true) }
  }

  private fun parseCondition(
    raw: JsonObject,
    negate: Boolean = false,
  ): (Player) -> Boolean {
    val skillName = parseString(raw, "skill")
      ?: throw NullPointerException("Expected skill to be defined for the condition")

    val skill =
      Skill.find<Any>(skillName)
        ?: throw NullPointerException("Expected skill $skillName to be registered for the condition")

    val action = parseString(raw, "action") ?: "can"
    if (action != "can" && action != "cannot") {
      throw RuntimeException("Expected skill action to be 'can', 'cannot', or not provided")
    }
    val isCannot = action == "cannot"

    val rawValue = getValue(raw, "value")
    val usedValue = when {
      rawValue == null -> null
      rawValue.isJsonPrimitive -> rawValue.asString
      else -> null
    }

    val value = usedValue?.let {
      SkillType.find(skill)?.castFromString(it)
        ?: throw NullPointerException("Expected value $usedValue to match skill's expected type")
    }

    return { player: Player ->
      val canResponse = PlayerApi.can(player, skillName, value)
      val maybeUnless = if (negate) !canResponse else canResponse

      if (isCannot) !maybeUnless else maybeUnless
    }
  }
}
