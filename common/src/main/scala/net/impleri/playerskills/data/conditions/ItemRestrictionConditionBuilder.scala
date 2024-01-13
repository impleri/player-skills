package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.restrictions.item.ItemConditions
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

case class ItemRestrictionConditionBuilder(
  name: ResourceLocation,
  protected val skillOps: SkillOps = Skill(),
  protected val skillTypeOps: SkillTypeOps = SkillType(),
  protected val playerOps: Player = Player(),
  protected val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionConditionsBuilder with ItemConditions {
  override def parseRestriction(jsonElement: JsonObject): Unit = {
    parseTarget(jsonElement, "item")
    isIdentifiable = parseBoolean(jsonElement, "identifiable", isIdentifiable)
    isHoldable = parseBoolean(jsonElement, "holdable", isHoldable)
    isWearable = parseBoolean(jsonElement, "wearable", isWearable)
    isUsable = parseBoolean(jsonElement, "usable", isUsable)
    isHarmful = parseBoolean(jsonElement, "harmful", isHarmful)
  }

  override def toggleEverything(): Unit = {
    isIdentifiable = Option(true)
    isHoldable = Option(true)
    isWearable = Option(true)
    isUsable = Option(true)
    isHarmful = Option(true)
  }

  override def toggleNothing(): Unit = {
    isIdentifiable = Option(false)
    isHoldable = Option(false)
    isWearable = Option(false)
    isUsable = Option(false)
    isHarmful = Option(false)
  }
}
