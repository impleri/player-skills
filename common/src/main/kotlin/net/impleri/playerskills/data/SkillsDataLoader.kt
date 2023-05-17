package net.impleri.playerskills.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.impleri.playerskills.skills.basic.BasicSkill
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

class SkillsDataLoader : SimpleJsonResourceReloadListener(Gson, "skills") {
  override fun apply(
    datamap: Map<ResourceLocation, JsonElement>,
    resourceManager: ResourceManager,
    profilerFiller: ProfilerFiller,
  ) {
    datamap.forEach { (name, json) ->
      try {
        parseSkill(name, json)?.let {
          PlayerSkills.LOGGER.info("Registering skill ${it.name}")
          Skill.modify(it)
        }
      } catch (e: Throwable) {
        PlayerSkills.LOGGER.warn("Could not parse skill details for $name")
        e.message?.let { PlayerSkills.LOGGER.error("$it.") }
        e.printStackTrace()
      }
    }
  }

  private fun parseSkill(name: ResourceLocation, json: JsonElement): Skill<*>? {
    val raw = json.asJsonObject

    // Required fields will throw an NPE if they cannot be parsed
    val type = getValue(raw, "type")?.asString?.let { SkillResourceLocation.of(it) }
      ?: throw NullPointerException("Could not detect skill type")

    // Optional fields will be nullish if not found in JSON
    val description = parseValue(raw, "description", { it.asString })
    val changesAllowed = parseValue(raw, "changesAllowed", { it.asInt }) ?: Skill.UNLIMITED_CHANGES
    val (notify, notifyString) = parseNotify(raw)

    return when (type) {
      BasicSkillType.NAME -> {
        val initialValue = parseValue(raw, "initialValue", { it.asBoolean })
        val options = parseOptions(raw) { it.asBoolean }
        val teamMode = parseTeamMode(raw)

        BasicSkill(name, initialValue, description, options, changesAllowed, teamMode, notify, notifyString)
      }

      NumericSkillType.NAME -> {
        val initialValue = parseValue(raw, "initialValue", { it.asDouble })
        val options = parseOptions(raw) { it.asDouble }
        val teamMode = parseTeamMode(raw)

        NumericSkill(name, initialValue, description, options, changesAllowed, teamMode, notify, notifyString)
      }

      TieredSkillType.NAME -> {
        val initialValue = parseValue(raw, "initialValue", { it.asString })
        val options = parseOptions(raw) { it.asString }
        val teamMode = parseTeamMode(raw, TeamMode.pyramid())

        TieredSkill(name, initialValue, description, options, changesAllowed, teamMode, notify, notifyString)
      }

      SpecializedSkillType.NAME -> {
        val initialValue = parseValue(raw, "initialValue", { it.asString })
        val options = parseOptions(raw) { it.asString }
        val teamMode = parseTeamMode(raw, TeamMode.splitEvenly())

        SpecializedSkill(
          name,
          initialValue,
          description,
          options,
          changesAllowed,
          teamMode,
          notify,
          notifyString,
        )
      }

      else -> {
        PlayerSkills.LOGGER.warn("Unknown skill type $type for $name")
        null
      }
    }
  }

  private fun getValue(
    raw: JsonObject,
    key: String,
  ): JsonElement? {
    return try {
      raw.get(key)
    } catch (e: NullPointerException) {
      PlayerSkills.LOGGER.info("Could not get value for $key")
      e.message?.let { PlayerSkills.LOGGER.error(it) }
      e.printStackTrace()
      null
    }
  }

  private fun <T> parseValue(
    raw: JsonObject,
    key: String,
    parser: ((element: JsonElement) -> T),
    defaultValue: T? = null,
  ): T? {
    val rawValue = getValue(raw, key) ?: return null

    return if (rawValue.isJsonNull) defaultValue else parser(rawValue)
  }

  private fun <T> parseOptions(
    raw: JsonObject,
    parser: (element: JsonElement) -> T,
  ): List<T>? {
    return parseValue(
      raw,
      "options",
      {
        it.asJsonArray.map { rawValue -> parser(rawValue) }.toList()
      },
    )
  }

  private fun createTeamMode(mode: String, rate: Double? = null): TeamMode {
    return when (mode) {
      "shared" -> TeamMode.shared()
      "splitEvenly" -> TeamMode.splitEvenly()
      "pyramid" -> TeamMode.pyramid()

      "limited" -> rate?.let { TeamMode.limited(it) }
        ?: throw NullPointerException("A rate is required for limited Team Mode")

      "proportional" -> rate?.let { TeamMode.proportional(it) }
        ?: throw NullPointerException("A rate is required for proportional Team Mode")

      else -> TeamMode.off()
    }
  }

  private fun restrictTeamMode(allowed: TeamMode? = null): (TeamMode) -> TeamMode {
    return {
      when {
        it.isPyramid && allowed?.isPyramid != true -> TeamMode.off()
        it.isSplitEvenly && allowed?.isSplitEvenly != true -> TeamMode.off()
        else -> it
      }
    }
  }

  private fun parseTeamMode(
    raw: JsonObject,
    allowed: TeamMode? = null,
  ): TeamMode {
    val rawMode = getValue(raw, "teamMode") ?: return TeamMode.off()

    val restrict = restrictTeamMode(allowed)

    if (rawMode.isJsonNull) {
      return TeamMode.off()
    }

    if (rawMode.isJsonObject) {
      val mode = parseValue(rawMode.asJsonObject, "mode", { it.asString }) ?: ""
      val rate = parseValue(rawMode.asJsonObject, "rate", { it.asDouble })

      return restrict(createTeamMode(mode, rate))
    }

    return restrict(createTeamMode(rawMode.asString, null))
  }

  private fun parseNotify(
    raw: JsonObject,
  ): Pair<Boolean, String?> {
    val notifyString = parseValue(
      raw,
      "notify",
      {
        try {
          it.asString
        } catch (_: Throwable) {
          null
        }
      },
    )

    if (!notifyString.isNullOrEmpty()) {
      return true to notifyString
    }

    val notify = parseValue(
      raw,
      "notify",
      { it.asBoolean },
    ) ?: false

    return notify to null
  }

  companion object {
    private val Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
  }
}
