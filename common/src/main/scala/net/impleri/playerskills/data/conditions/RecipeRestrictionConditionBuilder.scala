package net.impleri.playerskills.data.conditions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.core.{ResourceLocation => ResourceFacade}
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.restrictions.recipe.RecipeConditions
import net.impleri.playerskills.restrictions.recipe.RecipeTarget
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

case class RecipeRestrictionConditionBuilder(
  name: ResourceLocation,
  protected val skillOps: SkillOps = Skill(),
  protected val skillTypeOps: SkillTypeOps = SkillType(),
  protected val playerOps: Player = Player(),
  protected val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionConditionsBuilder with MultiTargetParser[RecipeTarget] with RecipeConditions {
  private def parseRecipe(element: JsonElement): Seq[RecipeTarget] = {
    val el = element.getAsJsonObject

    parseString(el, "type")
      .flatMap(ResourceFacade.apply(_, isSkill = false))
      .filter(Registry.RecipeTypes.isValid)
      .map(
        RecipeTarget(
          _,
          parseString(el, "output"),
          parseArray(el, "ingredients", castAsString),
        ),
      )
      .toSeq
  }

  override def parseRestriction(jsonElement: JsonObject): Unit = {
    targets = getTarget(jsonElement, "recipe").flatMap(parseRecipe)
    isProducible = parseBoolean(jsonElement, "producible", isProducible)
  }

  override def toggleEverything(): Unit = {
    isProducible = Option(true)
  }

  override def toggleNothing(): Unit = {
    isProducible = Option(false)
  }
}
