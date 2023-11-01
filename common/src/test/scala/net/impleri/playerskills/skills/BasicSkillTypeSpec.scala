package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.skills.basic.BasicSkill
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation

class BasicSkillTypeSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val skillName = SkillResourceLocation("test_skill").get

  private val dumbSkill = BasicSkill(skillName)
  private val truthySkill = BasicSkill(skillName, value = Option(true))
  private val falsySkill = BasicSkill(skillName, value = Option(false))

  "BasicSkillType" should "return a BasicSkillType instance" in {
    val facade = BasicSkillType()

    facade.isInstanceOf[BasicSkillType] should be(true)
  }

  "BasicSkillType.serialize" should "cast true boolean value to a string" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    val result = testUnit.serialize(truthySkill)
    val expected = List(skillName, BasicSkillType.NAME.toString, BasicSkillType.STRING_TRUE, Skill
      .UNLIMITED_CHANGES,
    ).mkString(";")

    result should be(expected)
  }

  it should "cast false boolean value to a string" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    val result = testUnit.serialize(falsySkill)
    val expected = List(skillName, BasicSkillType.NAME.toString, BasicSkillType.STRING_FALSE, Skill
      .UNLIMITED_CHANGES,
    ).mkString(";")

    result should be(expected)
  }

  "BasicSkillType.castFromString" should "cast true string to a boolean true value" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.castFromString(BasicSkillType.STRING_TRUE).value should be(true)
  }

  it should "cast any other string to a boolean false value" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.castFromString("mistake").value should be(false)
  }

  "BasicSkillType.can" should "pass if value is true" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(truthySkill) should be(true)
  }

  it should "pass if value is true and threshold is true" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(truthySkill, Option(true)) should be(true)
  }

  it should "fail if value is true and threshold is false" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(truthySkill, Option(false)) should be(false)
  }

  it should "fail if value is false" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(falsySkill) should be(false)
  }

  it should "fail if value is false and threshold is true" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(falsySkill, Option(true)) should be(false)
  }

  it should "pass if value is false and threshold is false" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(falsySkill, Option(false)) should be(true)
  }

  it should "fail if there is no value" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(dumbSkill) should be(false)
  }

  it should "fail if there is no value and threshold is true" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(dumbSkill, Option(true)) should be(false)
  }

  it should "pass if there is no value and threshold is false" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.can(dumbSkill, Option(false)) should be(true)
  }

  "BasicSkillType.getNextValue" should "return boolean true regardless of current value" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(truthySkill).value should be(true)
  }

  "BasicSkillType.getPrevValue" should "return boolean false regardless of current value" in {
    val testUnit = BasicSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(dumbSkill).value should be(false)
  }

  "BasicSkill.mutate" should "return a clone of the existing skill with new value and changed changes allowed" in {
    val expected = BasicSkill(skillName, changesAllowed = 64)

    truthySkill.mutate(None, 64) should be(expected)
  }

  "BasicSkill.getMessageKey" should "return the positive key if value is true" in {
    truthySkill.getMessageKey should be("playerskills.notify.basic_skill_enabled")
  }

  it should "return the negative key if value is false" in {
    falsySkill.getMessageKey should be("playerskills.notify.basic_skill_disabled")
  }

  it should "return the negative key if there is no value" in {
    dumbSkill.getMessageKey should be("playerskills.notify.basic_skill_disabled")
  }
}
