package net.impleri.playerskills.restrictions.conditions

trait DimensionConditions {
  var includeDimensions: Seq[String] = Seq.empty
  var excludeDimensions: Seq[String] = Seq.empty

  def inDimension(dimension: String): Unit = {
    includeDimensions = includeDimensions :+ dimension
  }

  def notInDimension(dimension: String): Unit = {
    excludeDimensions = excludeDimensions :+ dimension
  }
}
