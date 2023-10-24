package net.impleri.playerskills.api.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.skills.SkillTypeRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
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

  private case class TestSkill(
    override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
    override val options: List[String] = List.empty,
    override val value: Option[String] = None,
    override val announceChange: Boolean = false,
  ) extends Skill[String] with ChangeableSkillOps[String, TestSkill] {
    override val name: ResourceLocation = new ResourceLocation("skills", "testname")
    override val skillType: ResourceLocation = new ResourceLocation("skills", "test_type")

    override protected[playerskills] def mutate(
      value: Option[String],
      changesAllowed: Int,
    ): TestSkill = {
      copy(value = value, changesAllowed = changesAllowed)
    }
  }

  private val defaultType = TestSkillType()

  private val skillMock = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]

  private val registryMock = mock[SkillTypeRegistry]

  private val loggerMock = mock[PlayerSkillsLogger]

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
    }skilltest:skill_type${SkillType.stringValueSeparator}$defaultStringValue${SkillType.stringValueSeparator}${
      Skill
        .UNLIMITED_CHANGES
    }",
    )
  }

  "SerializableSkillType.derialize" should "makes a Skill with the correct value" in {
    val skillName = new ResourceLocation("skilltest", "skill")
    val rawValue = Option("value")
    val changes = 5

    skillOpsMock.get[String](skillName) returns Option(TestSkill())


    val result = defaultType.deserialize("skilltest:skill", rawValue, changes)

    result.value.value.value should be("value")
    result.value.changesAllowed should be(changes)
  }

  it should "return nothing if unable to parse the skill name" in {
    val result = defaultType.deserialize("skillTest:failedSkillName", None, Skill.UNLIMITED_CHANGES)

    result should be(None)
  }

  it should "return nothing if unable to find the skill" in {
    val skillName = new ResourceLocation("skilltest", "skill")

    skillOpsMock.get[String](skillName) returns None

    val result = defaultType.deserialize("skilltest:skill", None, Skill.UNLIMITED_CHANGES)

    result should be(None)
  }

  "SkillType.can" should "be true if there is a value and there is no threshold" in {
    val skill = TestSkill(value = Option("value"))

    defaultType.can(skill, None) should be(true)
  }

  it should "be true if there is a value and it matches the threshold" in {
    val skill = TestSkill(value = Option("value"))

    defaultType.can(skill, Option("value")) should be(true)
  }

  it should "be false if there is a value and it does not match the threshold" in {
    val skill = TestSkill(value = Option("value"))

    defaultType.can(skill, Option("not_this")) should be(false)
  }

  it should "be false if there is no value" in {
    val skill = TestSkill(value = None)

    defaultType.can(skill, None) should be(false)
  }

  "SkillTypeRegistryFacade.all" should "proxy SkillTypeRegistry.entries" in {
    val facade = new SkillTypeOps(registryMock, loggerMock)
    val expected = List(defaultType)

    registryMock.entries returns expected

    facade.all() should be(expected)
  }

  "SkillTypeRegistryFacade.get" should "proxy SkillTypeRegistry.find with a ResourceLocation" in {
    val facade = new SkillTypeOps(registryMock, loggerMock)
    val givenName = new ResourceLocation("skills", "test")
    val expected = None

    registryMock.find(givenName) returns expected

    facade.get(givenName) should be(expected)
  }

  it should "proxy SkillTypeRegistry.find with a string" in {
    val facade = new SkillTypeOps(registryMock, loggerMock)
    val givenName = new ResourceLocation("skills", "test")
    val expected = None

    registryMock.find(givenName) returns expected

    facade.get("skills:test") should be(expected)
  }

  it should "proxy SkillTypeRegistry.find with a class" in {
    val facade = new SkillTypeOps(registryMock, loggerMock)
    val givenName = new ResourceLocation("skills", "test_type")
    val expected = None
    val givenSkill = TestSkill()

    registryMock.find(givenName) returns expected

    facade.get(givenSkill) should be(expected)
  }

  "SkillTypeOps.serialize" should "makes a string with the value" in {
    val skillType = new ResourceLocation("skilltest", "skill_type")
    val mockType = mock[SkillType[String]]
    val testUnit = new SkillTypeOps(registryMock, loggerMock)

    skillMock.name returns new ResourceLocation("skilltest", "skill")
    skillMock.skillType returns skillType
    registryMock.find[String](skillType) returns Option(mockType)

    mockType.serialize(skillMock) returns "serialized-skill"

    testUnit.serialize(skillMock).value should be("serialized-skill")

    loggerMock.debugP(*)(*) was called
  }
}
