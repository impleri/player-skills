package net.impleri.playerskills.restrictions.conditions

import net.impleri.slab.resources.ResourceLocation

trait SingleTargetRestriction[T] {
  var target: Option[T] = None

  def isValid: Boolean =
    target.nonEmpty

  def getTarget: String = target.fold("")(_.toString)
}

trait MultiTargetRestriction[T] {
  var targets: Seq[T] = Seq.empty

  def isValid: Boolean =
    targets.nonEmpty

  def getTarget: String = targets.map(_.toString).mkString(", ")
}

trait RestrictionConditionsBuilder
    extends BiomeConditions
    with DimensionConditions
    with PlayerConditions {
  def name: ResourceLocation

  def isValid: Boolean

  def getTarget: String
}
