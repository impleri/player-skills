package net.impleri.playerskills.api.skills

import net.impleri.playerskills.BaseSpec

class TeamModeSpec extends BaseSpec {
  private case class TestSkill(
    override val value: Option[String] = None,
    override val options: List[String] = List.empty,
  ) extends Skill[String]

  private val testSkill = TestSkill()
  private val testSkillWithOptions = TestSkill(options = List("A", "B", "C", "D", "E")) // *, 16, 8, 4, 2

  "TeamMode.Off" should "echo the count as the current limit" in {
    val givenCount = 6
    TeamMode.Off().getLimit(testSkill, givenCount) should be(givenCount)
  }

  "TeamMode.Shared" should "echo the count as the current limit" in {
    val givenCount = 3
    TeamMode.Shared().getLimit(testSkill, givenCount) should be(givenCount)
  }

  "TeamMode.SplitEvenly" should "echo the count if there are no options on the skill" in {
    val givenCount = 3
    TeamMode.SplitEvenly().getLimit(testSkill, givenCount) should be(givenCount)
  }

  it should "derive the count from the skill's options" in {
    val givenCount = 10
    val expectedLimit = 2
    TeamMode.SplitEvenly().getLimit(testSkillWithOptions, givenCount) should be(expectedLimit)
  }

  it should "round up when uneven" in {
    val givenCount = testSkillWithOptions.options.size + 2
    val expectedLimit = 2
    TeamMode.SplitEvenly().getLimit(testSkillWithOptions, givenCount) should be(expectedLimit)
  }

  "TeamMode.Pyramid" should "generate appropriate option limits" in {
    val givenCount = 5
    val expectedMap = Map(
      1 -> 16,
      2 -> 8,
      3 -> 4,
      4 -> 2,
      5 -> 1, // This is a purposely inaccessible value in order to allow the first option value to be unlimited
    )
    TeamMode.Pyramid().getOptionLimits(givenCount) should be(expectedMap)
  }

  it should "generate small pyramids" in {
    val givenCount = 3
    val expectedMap = Map(
      1 -> 4,
      2 -> 2,
      3 -> 1, // This is a purposely inaccessible value in order to allow the first option value to be unlimited
    )
    TeamMode.Pyramid().getOptionLimits(givenCount) should be(expectedMap)
  }

  it should "generate large pyramids" in {
    val givenCount = 8
    val expectedMap = Map(
      1 -> 128,
      2 -> 64,
      3 -> 32,
      4 -> 16,
      5 -> 8,
      6 -> 4,
      7 -> 2,
      8 -> 1, // This is a purposely inaccessible value in order to allow the first option value to be unlimited
    )
    TeamMode.Pyramid().getOptionLimits(givenCount) should be(expectedMap)
  }

  it should "get the right size for the given index" in {
    TeamMode.Pyramid().getOptionLimit(testSkillWithOptions)(4).value should be(2)
  }

  it should "handle indices out of bounds" in {
    TeamMode.Pyramid().getOptionLimit(testSkillWithOptions)(6) should be(None)
  }

  it should "echo the count if the new value is the first option" in {
    val givenCount = 55
    TeamMode.Pyramid().getLimit(testSkillWithOptions.copy(value = Option("A")), givenCount) should be(givenCount)
  }

  it should "return 0 if the new value is not found in the options" in {
    val givenCount = 55
    TeamMode.Pyramid().getLimit(testSkillWithOptions.copy(value = Option("Z")), givenCount) should be(0)
  }

  it should "return 2 for the last option" in {
    val givenCount = 55
    TeamMode.Pyramid().getLimit(testSkillWithOptions.copy(value = Option("E")), givenCount) should be(2)
  }

  "TeamMode.Proportional" should "divide the count evenly across player count" in {
    val givenCount = 60
    TeamMode.Proportional(20).getLimit(testSkillWithOptions, givenCount) should be(12)
  }

  it should "round up if split across player count is not even" in {
    val givenCount = 30
    TeamMode.Proportional(14).getLimit(testSkillWithOptions, givenCount) should be(5)
  }

  "TeamMode.Limited" should "echo back the amount given" in {
    val givenCount = 30
    TeamMode.Limited(5).getLimit(testSkillWithOptions, givenCount) should be(5)
  }
}
