package net.impleri.playerskills.api.skills

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.ResourceLocation

class SkillTypeSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]

  private val defaultStringValue = "default-value"

  private case class TestSkillType() extends SkillType[String] {
    override def getPrevValue(
      skill: Skill[String],
      min: Option[String],
      max: Option[String],
    ): Option[String] = {
      Option("previous")
    }

    override def getNextValue(
      skill: Skill[String],
      min: Option[String],
      max: Option[String],
    ): Option[String] = {
      Option("next")
    }

    override protected def castToString(value: Option[String]): Option[String] = {
      value
        .orElse(Option(defaultStringValue))
    }

    override def castFromString(value: Option[String]): Option[String] = value.orElse(Option(defaultStringValue))

    override protected def skillOps: SkillOps = skillOpsMock
  }

  private val defaultType = TestSkillType()

  private val skillMock = mock[Skill[String]]

  "SerializableSkillType.serialize" should "makes a string with the value" in {
    skillMock.name returns new ResourceLocation("skilltest", "skill")
    skillMock.skillType returns new ResourceLocation("skilltest", "skill_type")
    skillMock.value returns Option("value")
    skillMock.changesAllowed returns Skill.UNLIMITED_CHANGES

    defaultType.serialize(skillMock) should be(s"skilltest:skill${
      SkillType
        .stringValueSeparator
    }skilltest:skill_type${SkillType.stringValueSeparator}value${SkillType.stringValueSeparator}${
      Skill
        .UNLIMITED_CHANGES
    }",
    )
  }

  it should "makes a string with default value" in {
    skillMock.name returns new ResourceLocation("skilltest", "skill")
    skillMock.skillType returns new ResourceLocation("skilltest", "skill_type")
    skillMock.value returns None
    skillMock.changesAllowed returns Skill.UNLIMITED_CHANGES

    defaultType.serialize(skillMock) should be(s"skilltest:skill${
      SkillType
        .stringValueSeparator
    }skilltest:skill_type${SkillType.stringValueSeparator}${defaultStringValue}${SkillType.stringValueSeparator}${
      Skill
        .UNLIMITED_CHANGES
    }",
    )
  }
}
