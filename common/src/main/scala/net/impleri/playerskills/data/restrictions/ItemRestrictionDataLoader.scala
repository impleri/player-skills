package net.impleri.playerskills.data.restrictions

import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.data.RestrictionDataLoader
import net.impleri.playerskills.data.conditions.ItemRestrictionConditionBuilder
import net.impleri.playerskills.restrictions.item.ItemRestrictionBuilder
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

case class ItemRestrictionDataLoader(
  protected val itemRestrictionBuilder: ItemRestrictionBuilder,
  override val skillOps: SkillOps = Skill(),
  override val skillTypeOps: SkillTypeOps = SkillType(),
  override val playerOps: Player = Player(),
  override val logger: Logger = PlayerSkillsLogger.PARSE,
) extends RestrictionDataLoader("item_restrictions") {
  override protected def parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ): Unit =
    ItemRestrictionConditionBuilder(name, skillOps, skillTypeOps, playerOps)
      .tap(logLoading(name))
      .tap(_.parse(jsonElement))
      .tap(logBuilder("item", name))
      .pipe(Option(_))
      .filter(_.isValid)
      .foreach(itemRestrictionBuilder.add(name.toString))

  override def save(): Unit = itemRestrictionBuilder.commit()
}
