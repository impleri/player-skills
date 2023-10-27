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
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  protected val skillOps: SkillOps = Skill(),
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
    val skillType = parseString(raw, "type").tap {
      case None => logger.warn("Tried to parse undefined skill type")
      case _ => ()
    }

    skillType
      .flatMap(SkillResourceLocation.apply)
      .flatMap {
        case BasicSkillType.NAME => Option(BasicSkill(
          value = parseBoolean(raw, "initialValue"),
          options = parseOptions(raw, _.getAsBoolean),
          teamMode = parseTeamMode(raw),
          name = name,
          description = description,
          changesAllowed = changesAllowed,
          announceChange = notify,
          notifyKey = notifyString,
        ),
        )

        case NumericSkillType.NAME => Option(NumericSkill(
          value = parseDouble(raw, "initialValue"),
          options = parseOptions(raw, _.getAsDouble),
          teamMode = parseTeamMode(raw),
          name = name,
          description = description,
          step = parseDouble(raw, "step").getOrElse(NumericSkill.DefaultStep),
          changesAllowed = changesAllowed,
          announceChange = notify,
          notifyKey = notifyString,
        ),
        )

        case TieredSkillType.NAME => Option(TieredSkill(
          value = parseString(raw, "initialValue"),
          options = parseOptions(raw, _.getAsString),
          teamMode = parseTeamMode(raw, Option(TeamMode.Pyramid())),
          name = name,
          description = description,
          changesAllowed = changesAllowed,
          announceChange = notify,
          notifyKey = notifyString,
        ),
        )

        case SpecializedSkillType.NAME => Option(SpecializedSkill(
          value = parseString(raw, "initialValue"),
          options = parseOptions(raw, _.getAsString),
          teamMode = parseTeamMode(raw, Option(TeamMode.SplitEvenly())),
          name = name,
          description = description,
          changesAllowed = changesAllowed,
          announceChange = notify,
          notifyKey = notifyString,
        ),
        )

        case _ =>
        logger.warn(s"Unknown skill $name with type $skillType")
        None
      }
  }


  private def createTeamMode(mode: String, rate: Option[Double] = None): Either[Exception, TeamMode] = {
    mode match {
      case "shared" => Right(TeamMode.Shared())
      case "splitEvenly" => Right(TeamMode.SplitEvenly())
      case "pyramid" => Right(TeamMode.Pyramid())
      case "limited" => {
        rate
          .map(_.floor.toInt)
          .map(TeamMode.Limited)
          .toRight(LimitRequiredForTeamMode())
      }
      case "proportional" => rate.map(TeamMode.Proportional).toRight(ProportionRequiredForTeamMode())
      case _ => Right(TeamMode.Off())
    }
  }

  private def restrictTeamMode(allowed: Option[TeamMode] = None)(mode: TeamMode): TeamMode = {
    allowed match {
      case Some(_: TeamMode.Pyramid) if mode == TeamMode.Pyramid() => TeamMode.Off()
      case Some(_: TeamMode.SplitEvenly) if mode == TeamMode.SplitEvenly() => TeamMode.Off()
      case _ => mode
    }
  }

  private def parseTeamMode(
    raw: JsonObject,
    allowed: Option[TeamMode] = None,
  ): TeamMode = {
    parseValue(raw, "teamMode", {
      case m if m.isJsonObject =>
      val mode = parseString(m.getAsJsonObject, "mode")
      val rate = parseDouble(m.getAsJsonObject, "rate")
      mode.flatMap(createTeamMode(_, rate).toOption)
      case s if s.isJsonPrimitive => createTeamMode(s.getAsString, None).toOption
      case _ => None
    },
    )
      .flatten
      .map(restrictTeamMode(allowed))
      .getOrElse(TeamMode.Off())
  }

  private def parseNotify(
    raw: JsonObject,
  ): (Boolean, Option[String]) = {
    parseString(raw, "notify") match {
      case Some(v) if v.nonEmpty => (true, Option(v))
      case _ => (parseBoolean(raw, "notify").getOrElse(false), None)
    }
  }
}

object SkillsDataLoader {
  private val GsonService: Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
}
