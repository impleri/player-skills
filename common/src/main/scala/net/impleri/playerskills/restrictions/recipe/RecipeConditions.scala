package net.impleri.playerskills.restrictions.recipe

import net.impleri.playerskills.restrictions.conditions.MultiTargetRestriction
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder

trait RecipeConditions extends RestrictionConditionsBuilder with MultiTargetRestriction[RecipeTarget] {
  var isProducible: Option[Boolean] = None

  def producible(): Unit = {
    isProducible = Option(true)
  }

  def unproducible(): Unit = {
    isProducible = Option(false)
  }

  def nothing(): Unit = {
    producible()
  }

  def everything(): Unit = {
    unproducible()
  }
}
