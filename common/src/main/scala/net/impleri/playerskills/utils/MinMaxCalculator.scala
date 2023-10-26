package net.impleri.playerskills.utils

object MinMaxCalculator {
  def calculate(
    a: Option[Double],
    b: Option[Double],
    comparator: (Double, Double) => Boolean,
  ): Option[Double] = {
    a.flatMap(aa => b.map(bb => if (comparator(aa, bb)) aa else bb).orElse(a))
  }

  val isGreaterThan: (Double, Double) => Boolean = (a: Double, b: Double) => a > b

  val isLessThan: (Double, Double) => Boolean = (a: Double, b: Double) => a < b

}
