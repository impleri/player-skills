package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.server.api.{Player => PlayerOps}

trait ConditionDataParser extends JsonDataParser {
  protected def skillOps: SkillOps

  protected def skillTypeOps: SkillTypeOps

  protected def playerOps: PlayerOps

  private def parseSkill[T](raw: JsonObject) = {
    parseString(raw, "skill")
      .flatMap(ResourceLocation(_, isSkill = false))
      .flatMap(skillOps.get[T])
  }

  private val ALLOWED_ACTIONS = Seq("can", "cannot")

  private def parseAction(raw: JsonObject) = {
    parseString(raw, "action")
      .filter(ALLOWED_ACTIONS.contains)
      .getOrElse("can")
  }

  private def parseCondition[T](
    raw: JsonObject,
    negate: Boolean = false,
  ): Player[_] => Boolean = {
    val skill = parseSkill[T](raw)
    val skillType = skill.flatMap(skillTypeOps.get[T])

    val action = parseAction(raw)
    val isCannot = action == "cannot"

    val rawValue = getElement(raw, "value").flatMap {
      case null => None
      case v: JsonElement if v.isJsonPrimitive => Option(v.getAsString).filterNot(_.isBlank)
    }

    val value = (skillType, rawValue) match {
      case (Some(t), Some(v)) => t.castFromString(v)
      case _ => None
    }

    (player: Player[_]) => {
      val can = skill.map(s => playerOps.can(player.uuid, s.name, value)) match {
        case Some(v) => if (negate) !v else v
        case _ => false
      }

      if (isCannot) !can else can
    }
  }

  protected def parseIf(raw: JsonObject): Seq[Player[_] => Boolean] = {
    parseObjectOrArray(raw, "if").map(e => parseCondition(e.getAsJsonObject))
  }

  protected def parseUnless(raw: JsonObject): Seq[Player[_] => Boolean] = {
    parseObjectOrArray(raw, "unless").map(e => parseCondition(e.getAsJsonObject, negate = true))
  }
}
