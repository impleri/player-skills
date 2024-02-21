package net.impleri.playerskills.api.restrictions

sealed trait RestrictionType

object RestrictionType {
  case class Item() extends RestrictionType

  case class Recipe() extends RestrictionType
}
