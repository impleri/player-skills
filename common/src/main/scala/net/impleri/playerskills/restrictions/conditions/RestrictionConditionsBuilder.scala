package net.impleri.playerskills.restrictions.conditions

import net.impleri.slab.resources.ResourceLocation

sealed trait TargetedRestriction {
  def isValid: Boolean

  def getTarget: String
}

trait SingleTargetRestriction[T] extends TargetedRestriction {
  var target: Option[T] = None

  override def isValid: Boolean =
    target.nonEmpty

  override def getTarget: String = target.fold("")(_.toString)
}

trait MultiTargetRestriction[T] extends TargetedRestriction {
  var targets: Seq[T] = Seq.empty

  override def isValid: Boolean =
    targets.nonEmpty

  override def getTarget: String = targets.map(_.toString).mkString(", ")
}

trait RestrictionConditionsBuilder
    extends BiomeConditions
    with DimensionConditions
    with PlayerConditions {
  def name: ResourceLocation
}
