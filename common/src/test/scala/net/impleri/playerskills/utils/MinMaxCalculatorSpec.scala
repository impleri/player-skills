package net.impleri.playerskills.utils

import net.impleri.playerskills.BaseSpec

class MinMaxCalculatorSpec extends BaseSpec {
  "MinMaxCalculatorSpec.calculate" should "return A value if comparator returns true" in {
    MinMaxCalculator.calculate(Option(20), Option(10), MinMaxCalculator.isGreaterThan).value should be(20)
  }

  it should "return A value if B is None" in {
    MinMaxCalculator.calculate(Option(20), None, (a, b) => a > b).value should be(20)
  }

  it should "return B value if comparator returns false" in {
    MinMaxCalculator.calculate(Option(20), Option(10), MinMaxCalculator.isLessThan).value should be(10)
  }

  it should "return None if A is None despite B having a value" in {
    MinMaxCalculator.calculate(None, Option(20), (a, b) => a > b) should be(None)
  }

  it should "return None if A and B are None" in {
    MinMaxCalculator.calculate(None, None, (a, b) => a > b) should be(None)
  }
}
