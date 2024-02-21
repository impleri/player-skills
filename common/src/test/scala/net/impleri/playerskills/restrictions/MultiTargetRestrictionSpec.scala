package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.conditions.MultiTargetRestriction

class MultiTargetRestrictionSpec extends BaseSpec {
  private case class TestConditionBuilder(
    t: Seq[String] = Seq.empty,
  ) extends MultiTargetRestriction[String] {
    targets = t
  }

  private val testUnit = TestConditionBuilder()

  "SingleTargetRestriction.isValid" should "return false if there is no target" in {
    testUnit.isValid shouldBe false
  }

  it should "return true if there is a target string" in {
    testUnit.targets = Seq("target")
    testUnit.isValid shouldBe true
  }
}
