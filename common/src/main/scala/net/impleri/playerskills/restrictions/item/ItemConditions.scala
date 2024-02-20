package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.impleri.playerskills.restrictions.conditions.SingleTargetRestriction

trait ItemConditions extends RestrictionConditionsBuilder with SingleTargetRestriction[String] {
  var isIdentifiable: Option[Boolean] = None
  var isHoldable: Option[Boolean] = None
  var isWearable: Option[Boolean] = None
  var isUsable: Option[Boolean] = None
  var isHarmful: Option[Boolean] = None

  def holdable(): Unit = {
    isHoldable = Option(true)
  }

  def unholdable(): Unit = {
    isHoldable = Option(false)
    harmless()
    unwearable()
    unusable()
  }

  def identifiable(): Unit = {
    isIdentifiable = Option(true)
  }

  def unidentifiable(): Unit = {
    isIdentifiable = Option(false)
  }

  def harmful(): Unit = {
    isHarmful = Option(true)
    holdable()
  }

  def harmless(): Unit = {
    isHarmful = Option(false)
  }

  def wearable(): Unit = {
    isWearable = Option(true)
    holdable()
  }

  def unwearable(): Unit = {
    isWearable = Option(false)
  }

  def usable(): Unit = {
    isUsable = Option(true)
    holdable()
  }

  def unusable(): Unit = {
    isUsable = Option(false)
  }

  def nothing(): Unit = {
    wearable()
    identifiable()
    harmful()
    usable()
  }

  def everything(): Unit = {
    unholdable()
    unidentifiable()
  }
}
