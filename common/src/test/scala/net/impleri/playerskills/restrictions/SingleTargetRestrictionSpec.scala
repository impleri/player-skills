package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.conditions.SingleTargetRestriction

class SingleTargetRestrictionSpec extends BaseSpec {
  private case class TestConditionBuilder(
    t: Option[String] = None,
  ) extends SingleTargetRestriction[String] {
    target = t
  }

  private val testUnit = TestConditionBuilder()

  "SingleTargetRestriction.isValid" should "return false if there is no target" in {
    testUnit.isValid shouldBe false
  }

  it should "return true if there is a target string" in {
    testUnit.target = Option("target")
    testUnit.isValid shouldBe true
  }
}
