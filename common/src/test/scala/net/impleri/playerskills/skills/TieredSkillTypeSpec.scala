package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation

class TieredSkillTypeSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val skillName = SkillResourceLocation("test_skill").get
  private val skillOptions = List("wood", "stone", "iron", "gold", "diamond", "netherite")
  private val skillValue = "iron"

  private val dumbSkill = TieredSkill(skillName)
  private val valuedSkill = TieredSkill(skillName, value = Option(skillValue), options = skillOptions)
  private val woodSkill = TieredSkill(skillName, value = Option("wood"), options = skillOptions)
  private val netheriteSkill = TieredSkill(skillName, value = Option("netherite"), options = skillOptions)

  private val testUnit = TieredSkillType(skillOpsMock, loggerMock)

  "TieredSkillType.serialize" should "cast value to a string" in {
    val result = testUnit.serialize(valuedSkill)

    val expected = List(
      skillName,
      TieredSkillType.NAME.toString,
      skillValue,
      Skill.UNLIMITED_CHANGES,
    ).mkString(";")

    result should be(expected)
  }

  "TieredSkillType.castFromString" should "return the same string" in {
    testUnit.castFromString(skillValue).value should be(skillValue)
  }

  "TieredSkillType.can" should "pass if value is set" in {
    testUnit.can(valuedSkill) should be(true)
  }

  it should "pass if value is at or above the index of the threshold" in {
    testUnit.can(valuedSkill, Option(skillValue)) should be(true)
  }

  it should "fail if value is below the index of the threshold" in {
    testUnit.can(valuedSkill, Option("gold")) should be(false)
  }

  it should "fail if there is no value" in {
    testUnit.can(dumbSkill) should be(false)
  }

  it should "fail if there is no value and threshold is set" in {
    testUnit.can(dumbSkill, Option(skillValue)) should be(false)
  }

  "TieredSkillType.getNextValue" should "return the next tier from the current value" in {
    testUnit.getNextValue(valuedSkill).value should be("gold")
  }

  it should "start from the min value if the current value is lesser" in {
    testUnit.getNextValue(woodSkill, min = Option("gold")).value should be("diamond")
  }

  it should "return the max value if the next value is greater" in {
    testUnit.getNextValue(valuedSkill, max = Option("iron")).value should be("iron")
  }

  "TieredSkillType.getPrevValue" should "return the previous tier current value" in {
    testUnit.getPrevValue(valuedSkill).value should be("stone")
  }

  it should "start from the max value if the current value is greater" in {
    testUnit.getPrevValue(netheriteSkill, max = Option("gold")).value should be("iron")
  }

  it should "return the min value if the next value is lesser" in {
    testUnit.getPrevValue(valuedSkill, min = Option("gold")).value should be("gold")
  }

  "TieredSkill.mutate" should "return a clone of the existing skill with new value and changed changes allowed" in {
    val expected = TieredSkill(skillName, changesAllowed = 64, options = skillOptions)

    valuedSkill.mutate(None, 64) should be(expected)
  }
}
