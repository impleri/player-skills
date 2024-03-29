package net.impleri.playerskills.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.data.utils.JsonDataParser
import net.impleri.playerskills.skills.basic.BasicSkill
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

case class LimitRequiredForTeamMode() extends Exception

case class ProportionRequiredForTeamMode() extends Exception

case class SkillsDataLoader(
  protected val skillOps: SkillOps = Skill(),
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
)
  extends SimpleJsonResourceReloadListener(SkillsDataLoader.GsonService, "skills") with JsonDataParser {
  override def apply(
    data: java.util.Map[ResourceLocation, JsonElement],
    resourceManager: ResourceManager,
    profilerFiller: ProfilerFiller,
  ): Unit = {
    CollectionConverters.asScala(data)
      .flatMap(t => parseSkill(t._1, t._2))
      .foreach(skillOps.upsert(_))
  }

  private def parseSkill(name: ResourceLocation, json: JsonElement): Option[Skill[_]] = {
    val raw = json.getAsJsonObject
    val description = parseString(raw, "description")
    val changesAllowed = parseInt(raw, "changesAllowed").getOrElse(Skill.UNLIMITED_CHANGES)
    val (notify, notifyString) = parseNotify(raw)
    val skillType = parseSkillType(raw)

    skillType.flatMap(SkillResourceLocation.apply)
      .flatMap {
        case BasicSkillType.NAME => createBasicSkill(raw, name, description, changesAllowed, notify, notifyString)
        case NumericSkillType.NAME => createNumericSkill(raw, name, description, changesAllowed, notify, notifyString)
        case TieredSkillType.NAME => createTieredSkill(raw, name, description, changesAllowed, notify, notifyString)
        case SpecializedSkillType.NAME => createSpecializedSkill(raw,
          name,
          description,
          changesAllowed,
          notify,
          notifyString,
        )
        case _ =>
        logger.warn(s"Unknown skill $name with type $skillType")
        None
      }
  }

  private def createBasicSkill(
    raw: JsonObject,
    name: ResourceLocation,
    description: Option[String],
    changesAllowed: Int,
    announceChange: Boolean,
    notifyString: Option[String],
  ): Option[BasicSkill] = {
    Option(
      BasicSkill(
        value = parseBoolean(raw, "initialValue"),
        options = parseOptions(raw, castAsBoolean),
        teamMode = parseTeamMode(raw),
        name = name,
        description = description,
        changesAllowed = changesAllowed,
        announceChange = announceChange,
        notifyKey = notifyString,
      ),
    )
  }

  private def createNumericSkill(
    raw: JsonObject,
    name: ResourceLocation,
    description: Option[String],
    changesAllowed: Int,
    announceChange: Boolean,
    notifyString: Option[String],
  ): Option[NumericSkill] = {
    Option(
      NumericSkill(
        value = parseDouble(raw, "initialValue"),
        options = parseOptions(raw, castAsDouble),
        teamMode = parseTeamMode(raw),
        name = name,
        description = description,
        step = parseDouble(raw, "step").getOrElse(NumericSkill.DefaultStep),
        changesAllowed = changesAllowed,
        announceChange = announceChange,
        notifyKey = notifyString,
      ),
    )
  }

  private def createTieredSkill(
    raw: JsonObject,
    name: ResourceLocation,
    description: Option[String],
    changesAllowed: Int,
    announceChange: Boolean,
    notifyString: Option[String],
  ): Option[TieredSkill] = {
    Option(
      TieredSkill(
        value = parseString(raw, "initialValue"),
        options = parseOptions(raw, castAsString),
        teamMode = parseTeamMode(raw, Option(TeamMode.Pyramid())),
        name = name,
        description = description,
        changesAllowed = changesAllowed,
        announceChange = announceChange,
        notifyKey = notifyString,
      ),
    )
  }

  private def createSpecializedSkill(
    raw: JsonObject,
    name: ResourceLocation,
    description: Option[String],
    changesAllowed: Int,
    announceChange: Boolean,
    notifyString: Option[String],
  ): Option[SpecializedSkill] = {
    Option(
      SpecializedSkill(
        value = parseString(raw, "initialValue"),
        options = parseOptions(raw, castAsString),
        teamMode = parseTeamMode(raw, Option(TeamMode.SplitEvenly())),
        name = name,
        description = description,
        changesAllowed = changesAllowed,
        announceChange = announceChange,
        notifyKey = notifyString,
      ),
    )
  }

  private def createTeamMode(mode: String, rate: Option[Double]): Either[Exception, TeamMode] = {
    mode match {
      case "shared" => Right(TeamMode.Shared())
      case "splitEvenly" => Right(TeamMode.SplitEvenly())
      case "pyramid" => Right(TeamMode.Pyramid())
      case "limited" => {
        rate.map(_.floor.toInt)
          .map(TeamMode.Limited)
          .toRight(LimitRequiredForTeamMode())
      }
      case "proportional" => rate.map(TeamMode.Proportional).toRight(ProportionRequiredForTeamMode())
      case _ => Right(TeamMode.Off())
    }
  }

  private def restrictTeamMode(allowed: Option[TeamMode])(mode: TeamMode): TeamMode = {
    mode match {
      case _: TeamMode.Pyramid if !allowed.contains(TeamMode.Pyramid()) => TeamMode.Off()
      case _: TeamMode.SplitEvenly if !allowed.contains(TeamMode.SplitEvenly()) => TeamMode.Off()
      case _ => mode
    }
  }

  private def parseSkillType(raw: JsonObject): Option[String] = {
    parseString(raw, "type").tap {
      case None => logger.warn("Tried to parse undefined skill type")
      case _ => ()
    }
  }

  private def parseTeamMode(
    raw: JsonObject,
    allowed: Option[TeamMode] = None,
  ): TeamMode = {
    parseValue(
      raw,
      "teamMode",
      {
        case m if m.isJsonObject =>
        val mode = parseString(m.getAsJsonObject, "mode")
        val rate = parseDouble(m.getAsJsonObject, "rate")
        mode.flatMap(createTeamMode(_, rate).toOption)
        case s if isPrimitiveType(s, _.isString) => createTeamMode(s.getAsString, None).toOption
        case _ => None
      },
    )
      .map(restrictTeamMode(allowed))
      .getOrElse(TeamMode.Off())
  }

  private def parseNotify(
    raw: JsonObject,
  ): (Boolean, Option[String]) = {
    parseBoolean(raw, "notify") match {
      case Some(v) => (v, None)
      case _ => parseNotifyString(raw)
    }
  }

  private def parseNotifyString(
    raw: JsonObject,
  ): (Boolean, Option[String]) = {
    parseString(raw, "notify") match {
      case Some(v) => (true, Option(v))
      case _ => (false, None)
    }
  }
}

object SkillsDataLoader {
  private[data] val GsonService: Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
}
