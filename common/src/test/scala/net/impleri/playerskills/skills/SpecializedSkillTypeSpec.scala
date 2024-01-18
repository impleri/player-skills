package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.impleri.playerskills.skills.specialized.SpecializedSkillType

class SpecializedSkillTypeSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]

  private val skillName = ResourceLocation("test_skill").get
  private val skillValue = "test-value"

  private val dumbSkill = SpecializedSkill(skillName)
  private val valuedSkill = SpecializedSkill(skillName, value = Option(skillValue))

  "SpecializedSkillType" should "return a SpecializedSkillType instance" in {
    val facade = SpecializedSkillType()

    facade.isInstanceOf[SpecializedSkillType] should be(true)
  }

  "SpecializedSkillType.serialize" should "cast value to a string" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    val result = testUnit.serialize(valuedSkill)
    val expected = List(skillName, SpecializedSkillType.NAME.toString, skillValue, Skill
      .UNLIMITED_CHANGES,
    ).mkString(";")

    result should be(expected)
  }

  "SpecializedSkillType.castFromString" should "return the same string" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.castFromString(skillValue).value should be(skillValue)
  }

  "SpecializedSkillType.can" should "pass if value is set" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.can(valuedSkill) should be(true)
  }

  it should "pass if value matches the threshold" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.can(valuedSkill, Option(skillValue)) should be(true)
  }

  it should "fail if value does not match the threshold" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.can(valuedSkill, Option("other-value")) should be(false)
  }

  it should "fail if there is no value" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.can(dumbSkill) should be(false)
  }

  it should "fail if there is no value and threshold is set" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.can(dumbSkill, Option(skillValue)) should be(false)
  }

  "SpecializedSkillType.getNextValue" should "return None regardless of current value" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.getNextValue(valuedSkill) should be(None)
  }

  "SpecializedSkillType.getPrevValue" should "return None regardless of current value" in {
    val testUnit = SpecializedSkillType(skillOpsMock)

    testUnit.getPrevValue(dumbSkill) should be(None)
  }

  "SpecializedSkill.mutate" should "return a clone of the existing skill with new value and changed changes allowed" in {
    val expected = SpecializedSkill(skillName, changesAllowed = 64)

    valuedSkill.mutate(None, 64) should be(expected)
  }

  "SpecializedSkill.getMessageKey" should "return the set key if value is set" in {
    valuedSkill.getMessageKey should be("playerskills.notify.specialized_skill_selected")
  }

  it should "return the empty key if value is None" in {
    dumbSkill.getMessageKey should be("playerskills.notify.specialized_skill_empty")
  }
}
