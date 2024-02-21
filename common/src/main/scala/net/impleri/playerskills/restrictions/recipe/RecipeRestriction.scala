package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.crafting.Recipe

case class RecipeRestriction(
  target: Recipe[_],
  condition: Player[_] => Boolean = Restriction.DEFAULT_CONDITION,
  replacement: Option[Recipe[_]] = None,
  includeDimensions: Seq[String] = Seq.empty,
  excludeDimensions: Seq[String] = Seq.empty,
  includeBiomes: Seq[String] = Seq.empty,
  excludeBiomes: Seq[String] = Seq.empty,
  producible: Boolean = false,
) extends Restriction[Recipe[_]] {
  override val restrictionType: RestrictionType = RestrictionType.Recipe()
}

object RecipeRestriction {
  def apply(target: Recipe[_], builder: RecipeConditions): RecipeRestriction = {
    new RecipeRestriction(
      target,
      builder.condition,
      None,
      builder.includeDimensions,
      builder.excludeDimensions,
      builder.includeBiomes,
      builder.excludeBiomes,
      builder.isProducible.getOrElse(Restriction.DEFAULT_RESPONSE),
    )
  }
}
