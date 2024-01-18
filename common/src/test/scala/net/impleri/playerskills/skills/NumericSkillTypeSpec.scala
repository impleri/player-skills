package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.playerskills.utils.PlayerSkillsLogger

class NumericSkillTypeSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val skillName = ResourceLocation("test_skill").get

  private val dumbSkill = NumericSkill(skillName)
  private val simpleValue = 15
  private val simpleSkill = NumericSkill(skillName, value = Option(simpleValue))
  private val stepValue = 10.3
  private val stepStep = 0.1
  private val stepChanges = 20
  private val stepSkill = NumericSkill(skillName,
    value = Option(stepValue),
    step = stepStep,
    changesAllowed = stepChanges,
  )

  "NumericSkillType" should "return a NumericSkillType instance" in {
    val facade = NumericSkillType()

    facade.isInstanceOf[NumericSkillType] should be(true)
  }

  "NumericSkillType.serialize" should "cast integer value to a string" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    val result = testUnit.serialize(simpleSkill)
    val expected = List(
      skillName.toString,
      NumericSkillType.NAME.toString,
      simpleValue.toDouble,
      Skill.UNLIMITED_CHANGES,
    ).mkString(";")

    result should be(expected)
  }

  it should "cast double value to a string" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    val result = testUnit.serialize(stepSkill)
    val expected = List(
      skillName.toString,
      NumericSkillType.NAME.toString,
      stepValue,
      stepChanges,
    ).mkString(";")

    result should be(expected)
  }

  "NumericSkillType.castFromString" should "cast numeric string to a numeric value" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.castFromString("153").value should be(153)
  }

  it should "cast any other string to None" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.castFromString("mistake") should be(None)
  }

  "NumericSkillType.can" should "pass if value is set and there is no threshold" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.can(simpleSkill) should be(true)
    loggerMock.debugP(*)(*) was called
  }

  it should "pass if value is greater than or equal to the threshold" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.can(simpleSkill, Option(simpleValue)) should be(true)
  }

  it should "fail if value is less than the threshold" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.can(simpleSkill, Option(simpleValue * 2)) should be(false)
  }

  it should "fail if there is no value nor a threshold" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.can(dumbSkill) should be(false)
  }

  it should "fail if there is no value and a threshold is set" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.can(dumbSkill, Option(simpleValue)) should be(false)
  }

  "NumericSkillType.getNextValue" should "return the next step value from the current value" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(simpleSkill).value should be(simpleValue + NumericSkill.DefaultStep)
  }

  it should "start with the min value if current value is lesser" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(simpleSkill, min = Option(simpleValue * 2)).value should be((simpleValue * 2) + NumericSkill
      .DefaultStep,
    )
  }

  it should "return the max value if current value + step would be greater" in {
    val maxValue = 15.5
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(simpleSkill, max = Option(maxValue)).value should be(maxValue)
  }

  it should "return None if no value is set" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(dumbSkill) should be(None)
  }

  it should "return None if no value is set even if min and max are set" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getNextValue(dumbSkill, min = Option(10), max = Option(20)) should be(None)
  }

  "NumericSkillType.getPrevValue" should "return the next step value from the current value" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(stepSkill).value should be(stepValue - stepStep)
  }

  it should "start with the max value if current value is higher" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(stepSkill, max = Option(stepValue / 2)).value should be((stepValue / 2) - stepStep)
  }

  it should "return the min value if current value - step would be lower" in {
    val minValue = 12.5
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(stepSkill, min = Option(minValue)).value should be(minValue)
  }

  it should "return None if no value is set" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(dumbSkill) should be(None)
  }

  it should "return None if no value is set even if min and max are set" in {
    val testUnit = NumericSkillType(skillOpsMock, loggerMock)

    testUnit.getPrevValue(dumbSkill, min = Option(10), max = Option(20)) should be(None)
  }


  "NumericSkill.mutate" should "return a clone of the existing skill with new value and changed changes allowed" in {
    val expected = NumericSkill(skillName, changesAllowed = 64)

    simpleSkill.mutate(None, 64) should be(expected)
  }
}
