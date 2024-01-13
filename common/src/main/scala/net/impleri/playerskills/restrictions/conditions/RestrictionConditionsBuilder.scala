package net.impleri.playerskills.restrictions.conditions

import net.minecraft.resources.ResourceLocation

trait RestrictionConditionsBuilder extends BiomeConditions with DimensionConditions with PlayerConditions {
  def name: ResourceLocation

  var target: Option[String] = None

  def isValid: Boolean = {
    target.nonEmpty
  }
}
