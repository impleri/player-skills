package net.impleri.playerskills.data.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation

trait ConditionDataParser extends JsonDataParser {
  protected def skillOps: SkillOps

  protected def skillTypeOps: SkillTypeOps

  protected def playerOps: PlayerOps

  private def parseSkill[T](raw: JsonObject) =
    parseString(raw, ConditionDataParser.SKILL_PROPERTY)
      .flatMap(ResourceLocation(_))
      .flatMap(skillOps.get[T])

  private def parseAction(raw: JsonObject) =
    parseString(raw, ConditionDataParser.ACTION_PROPERTY)
      .filter(ConditionDataParser.ALLOWED_ACTIONS.contains)
      .getOrElse(ConditionDataParser.CAN)

  private def parseCondition[T](negate: Boolean = false)(
    rawElement: JsonElement,
  ): Player => Boolean = {
    val raw = rawElement.getAsJsonObject
    val skillOpt = parseSkill[T](raw)

    val value = for {
      skillType <- skillOpt.flatMap(skillTypeOps.get[T])
      rawValue <- getElement(raw, ConditionDataParser.VALUE_PROPERTY).filter(_.isJsonPrimitive)
      valueString <- Option(rawValue.getAsString).filterNot(_.isBlank)
      castValue <- skillType.castFromString(valueString)
    } yield castValue

    val isCannot = parseAction(raw) == ConditionDataParser.CANNOT

    (player: Player) => {
      val can = skillOpt.fold(Restriction.DEFAULT_CONDITION_RESPONSE)(
        s => playerOps.can(player.uuid, s.name, value)
      )

      (negate, isCannot) match {
        case (false, false) => can  // No negation
        case (true, true)   => can  // Double negation
        case _              => !can // Single negation
      }
    }
  }

  protected def parseIf(raw: JsonObject): Seq[Player => Boolean] =
    parseObjectOrArray(raw, ConditionDataParser.IF_PROPERTY).map(parseCondition())

  protected def parseUnless(raw: JsonObject): Seq[Player => Boolean] =
    parseObjectOrArray(raw, ConditionDataParser.UNLESS_PROPERTY).map(parseCondition(negate = true))
}

object ConditionDataParser {
  private val CAN = "can"
  private val CANNOT = "cannot"
  private val ALLOWED_ACTIONS = Seq(CAN, CANNOT)

  private val ACTION_PROPERTY = "action"
  private val SKILL_PROPERTY = "skill"
  private val VALUE_PROPERTY = "value"
  private val IF_PROPERTY = "if"
  private val UNLESS_PROPERTY = "unless"
}
