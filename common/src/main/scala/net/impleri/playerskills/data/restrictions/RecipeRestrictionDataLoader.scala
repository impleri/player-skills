package net.impleri.playerskills.data.restrictions

import com.google.gson.JsonObject
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.data.RestrictionDataLoader
import net.impleri.playerskills.data.conditions.RecipeRestrictionConditionBuilder
import net.impleri.playerskills.restrictions.recipe.RecipeRestrictionBuilder
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

case class RecipeRestrictionDataLoader(
  protected val recipeRestrictionBuilder: RecipeRestrictionBuilder,
  override val skillOps: SkillOps = Skill(),
  override val skillTypeOps: SkillTypeOps = SkillType(),
  override val playerOps: Player = Player(),
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
) extends RestrictionDataLoader("recipe_restrictions") {
  override protected def parseRestriction(
    name: ResourceLocation,
    jsonElement: JsonObject,
  ): Unit = {
    val builder = RecipeRestrictionConditionBuilder(name)
    builder.parse(jsonElement)

    if (builder.isValid) {
      recipeRestrictionBuilder.add(builder)
    }
  }
}
