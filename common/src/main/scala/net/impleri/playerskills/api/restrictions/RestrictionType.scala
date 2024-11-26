package net.impleri.playerskills.api.restrictions

sealed trait RestrictionType

object RestrictionType {
  final case object Item extends RestrictionType

  final case object Recipe extends RestrictionType
}
