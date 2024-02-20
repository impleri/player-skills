package net.impleri.playerskills.data.conditions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.data.utils.BiomeFacetParser
import net.impleri.playerskills.data.utils.ConditionDataParser
import net.impleri.playerskills.data.utils.DimensionFacetParser
import net.impleri.playerskills.data.utils.JsonDataParser
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.restrictions.conditions.{RestrictionConditionsBuilder => ParentBuilder}
import net.impleri.playerskills.restrictions.conditions.MultiTargetRestriction
import net.impleri.playerskills.restrictions.conditions.SingleTargetRestriction

trait SingleTargetParser[T] extends JsonDataParser with SingleTargetRestriction[T] {
  protected[conditions] def getTarget(raw: JsonObject, key: String = "target"): Option[String] = {
    parseValue(raw, key, v => Option(v.getAsString))
  }
}

trait MultiTargetParser[T] extends JsonDataParser with MultiTargetRestriction[T] {
  protected[conditions] def getTarget(raw: JsonObject, key: String = "target"): Seq[JsonElement] = {
    getObject(raw, key).filter(_.isJsonObject).toSeq
  }
}

trait RestrictionConditionsBuilder extends ParentBuilder
  with JsonDataParser
  with BiomeFacetParser
  with DimensionFacetParser
  with ConditionDataParser {
  def parse(
    jsonElement: JsonObject,
  ): Unit = {
    parseDimensions(jsonElement, d => inDimension(d.getAsString), d => notInDimension(d.getAsString))
    parseBiomes(jsonElement, d => inBiome(d.getAsString), d => notInBiome(d.getAsString))
    parseCondition(jsonElement)
    parseRestriction(jsonElement)
    parseEverything(jsonElement)
    parseNothing(jsonElement)
  }

  private def parseCondition(raw: JsonObject): Unit = {
    val conditions = parseIf(raw)
    val unless = parseUnless(raw)

    condition = (player: Player[_]) => conditions.forall(_(player)) && unless.forall(_(player))
  }

  private def parseEverything(raw: JsonObject): Unit = {
    if (parseBoolean(raw, "everything").getOrElse(false)) {
      toggleEverything()
    }
  }

  private def parseNothing(raw: JsonObject): Unit = {
    if (parseBoolean(raw, "nothing").getOrElse(false)) {
      toggleNothing()
    }
  }

  def parseRestriction(jsonElement: JsonObject): Unit

  def toggleEverything(): Unit

  def toggleNothing(): Unit
}
