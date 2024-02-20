package net.impleri.playerskills.restrictions.conditions

import net.minecraft.resources.ResourceLocation

trait SingleTargetRestriction[T] {
  var target: Option[T] = None

  def isValid: Boolean = {
    target.nonEmpty
  }
}

trait MultiTargetRestriction[T] {
  var targets: Seq[T] = Seq.empty

  def isValid: Boolean = {
    targets.nonEmpty
  }
}

trait RestrictionConditionsBuilder extends BiomeConditions with DimensionConditions with PlayerConditions {
  def name: ResourceLocation
}
