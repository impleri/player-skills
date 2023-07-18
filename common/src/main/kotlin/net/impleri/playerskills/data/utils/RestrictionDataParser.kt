package net.impleri.playerskills.data.utils

import com.google.gson.JsonObject
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.minecraft.world.entity.player.Player
import net.impleri.playerskills.api.Player as PlayerApi

interface RestrictionDataParser : JsonDataParser {
  fun parseDimensions(
    raw: JsonObject,
    includeDimension: (biome: String) -> Unit,
    excludeDimension: (biome: String) -> Unit,
  ) {
    getValue(raw, "dimensions")?.let {
      when {
        it.isJsonObject -> {
          parseIncludeAction(it, includeDimension)
          parseExcludeAction(it, excludeDimension)
        }

        it.isJsonArray -> it.asJsonArray.forEach { includeDimension(it.asString) }
        else -> null
      }
    }
  }

  fun parseBiomes(raw: JsonObject, includeBiome: (biome: String) -> Unit, excludeBiome: (biome: String) -> Unit) {
    getValue(raw, "biomes")?.let {
      when {
        it.isJsonObject -> {
          parseIncludeAction(it, includeBiome)
          parseExcludeAction(it, excludeBiome)
        }

        it.isJsonArray -> it.asJsonArray.forEach { includeBiome(it.asString) }
        else -> null
      }
    }
  }

  fun parseIf(raw: JsonObject): List<(Player) -> Boolean> {
    val block = getValue(raw, "if") ?: return ArrayList()

    return when {
      block.isJsonArray -> block.asJsonArray.map {
        parseCondition(it.asJsonObject) { player, skill, value ->
          PlayerApi.can(
            player,
            skill,
            value,
          )
        }
      }

      block.isJsonObject -> listOf(
        parseCondition(block.asJsonObject) { player, skill, value ->
          PlayerApi.can(
            player,
            skill,
            value,
          )
        },
      )

      else -> ArrayList()
    }
  }

  fun parseUnless(raw: JsonObject): List<(Player) -> Boolean> {
    val block = getValue(raw, "unless") ?: return ArrayList()

    return when {
      block.isJsonArray -> block.asJsonArray.map {
        parseCondition(it.asJsonObject) { player, skill, value ->
          !PlayerApi.can(
            player,
            skill,
            value,
          )
        }
      }

      block.isJsonObject -> listOf(
        parseCondition(block.asJsonObject) { player, skill, value ->
          !PlayerApi.can(
            player,
            skill,
            value,
          )
        },
      )

      else -> ArrayList()
    }
  }

  private fun parseCondition(raw: JsonObject, callback: (Player, String, Any?) -> Boolean): (Player) -> Boolean {
    val skillName = parseString(raw, "skill")
      ?: throw NullPointerException("Expected skill to be defined for the condition")

    val skill =
      Skill.find<Any>(skillName)
        ?: throw NullPointerException("Expected skill $skillName to be registered for the condition")

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

    return { player: Player -> callback(player, skillName, value) }
  }
}
