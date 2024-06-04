package net.impleri.playerskills.api.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.slab.chat.Message
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.network.chat.contents.TranslatableContents

class SkillSpec extends BaseSpec {
  private case class TestSkill(
    override val changesAllowed: Int = Skill.UNLIMITED_CHANGES,
    override val options: List[String] = List.empty,
    override val value: Option[String] = None,
    override val announceChange: Boolean = false,
  ) extends Skill[String] with ChangeableSkillOps[String, TestSkill] {
    override val name: ResourceLocation = ResourceLocation("skills", "testname").get
    override val skillType: ResourceLocation = ResourceLocation("skills", "test_type").get

    override protected[playerskills] def mutate(
      value: Option[String],
      changesAllowed: Int,
    ): TestSkill = {
      copy(value = value, changesAllowed = changesAllowed)
    }
  }

  private val testValue = "testvalue"
  private val secondValue = "second"
  private val disallowedValue = "nope"

  private val unlimited = TestSkill()
  private val restricted = TestSkill(changesAllowed = Skill.UNLIMITED_CHANGES, options = List(testValue, secondValue))
  private val valued = TestSkill(value = Option(testValue), announceChange = true)
  private val oneChange = TestSkill(changesAllowed = 1, value = Option(secondValue))
  private val noChanges = TestSkill(changesAllowed = 0)

  private val skillRegistryMock = mock[SkillRegistry]
  private val loggerMock = mock[Logger]
  private val skillTypeOpsMock = mock[SkillTypeOps]
  private val skillTypeMock = mock[SkillType[String]]

  "ChangeableSkill.areChangesAllowed" should "allow changes by default" in {
    unlimited.areChangesAllowed() shouldBe true
  }

  it should "allow changes if more than 0 changes remain" in {
    oneChange.areChangesAllowed() shouldBe true
  }

  it should "disallow changes if 0 changes remain" in {
    noChanges.areChangesAllowed() shouldBe false
  }

  "ChangeableSkill.isAllowedValue" should "allow values if no options exist" in {
    unlimited.isAllowedValue(Option(testValue)) shouldBe true
  }

  it should "allow values on the list of allowed options" in {
    restricted.isAllowedValue(Option(testValue)) shouldBe true
  }

  it should "allow unsetting values despite the list of allowed options" in {
    restricted.isAllowedValue(None) shouldBe true
  }

  it should "disallow values not on the list of allowed options" in {
    restricted.isAllowedValue(Option(disallowedValue)) shouldBe false
  }

  "ChangeableSkillOps.mutate" should "return a new skill with the specified changes" in {
    val newChanges = 4
    val received = oneChange.mutate(None, newChanges)

    received.changesAllowed shouldBe newChanges
    received.value shouldNot be(Option(secondValue))
    received.value shouldBe None
  }

  it should "return a new skill with the change reduced automatically" in {
    val received = oneChange.mutate(Option(disallowedValue))

    received.changesAllowed shouldBe 0
    received.value shouldBe Option(disallowedValue)
  }

  "TranslatableSkill.getNotification" should "return nothing if not announcing the change" in {
    val skill = TestSkill()
    val received = skill.getNotification()

    skill.announceChange shouldBe false
    received shouldBe None
  }

  it should "returns a component for rendering" in {
    val received = valued.getNotification()

    valued.announceChange shouldBe true
    received.value.output.getString shouldBe "playerskills.notify.skill_change"
    received.value.output.getContents.isInstanceOf[TranslatableContents] should be(true)
    received
      .value
      .output
      .getContents
      .asInstanceOf[TranslatableContents]
      .getArgs
      .map(_.asInstanceOf[Message.Any].output.getString) shouldBe Seq("testname", testValue, "").toArray
  }

  "SkillRegistryFacade.all" should "proxy SkillRegistry.entries" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)
    val expected = List(unlimited)

    skillRegistryMock.entries returns expected

    facade.all() shouldBe expected
  }

  "SkillRegistryFacade.get" should "proxy SkillRegistry.find" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)
    val givenName = ResourceLocation("skills", "test").get
    val expected = None

    skillRegistryMock.find(givenName) returns expected

    facade.get(givenName) shouldBe expected
  }

  "SkillRegistryFacade.upsert" should "proxy SkillRegistry.upsert" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    facade.upsert(unlimited)

    skillRegistryMock.upsert(unlimited) was called
    loggerMock.info(*) was called
  }

  "SkillRegistryFacade.remove" should "proxy SkillRegistry.remove" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    facade.remove(unlimited)

    skillRegistryMock.removeSkill(unlimited) was called
  }

  "SkillOps.calculatePrev" should "proxy SkillType.getPrevValue" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    val expected = Option(testValue)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)
    skillTypeMock.getPrevValue(unlimited, Option("param1"), Option("param2")) returns expected

    facade.calculatePrev(unlimited, Option("param1"), Option("param2")) should be(expected)
  }

  it should "proxy SkillType.getPrevValue with default values" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    val expected = Option(testValue)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)
    skillTypeMock.getPrevValue(unlimited, None, None) returns expected

    facade.calculatePrev(unlimited) should be(expected)
  }

  "SkillOps.calculateNext" should "proxy SkillType.getNextValue" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    val expected = Option(testValue)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)
    skillTypeMock.getNextValue(unlimited, Option("param1"), Option("param2")) returns expected

    facade.calculateNext(unlimited, Option("param1"), Option("param2")) shouldBe expected
  }

  it should "proxy SkillType.getNextValue with defaults" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    val expected = Option(testValue)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)
    skillTypeMock.getNextValue(unlimited, None, None) returns expected

    facade.calculateNext(unlimited) shouldBe expected
  }

  "SkillOps.sortHelper" should "sort A before B if can A && cannot B" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)

    skillTypeMock.can(unlimited, valued.value) returns true
    skillTypeMock.can(valued, unlimited.value) returns false

    facade.sortHelper(unlimited, valued) shouldBe -1
  }

  it should "sort B before A if can B && cannot A" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)

    skillTypeMock.can(unlimited, valued.value) returns false
    skillTypeMock.can(valued, unlimited.value) returns true

    facade.sortHelper(unlimited, valued) shouldBe 1
  }

  it should "sort A and B equally if can A & B" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    skillTypeOpsMock.get(unlimited) returns Option(skillTypeMock)

    skillTypeMock.can(unlimited, valued.value) returns true
    skillTypeMock.can(valued, unlimited.value) returns true

    facade.sortHelper(unlimited, valued) shouldBe 0
  }

  it should "sort A and B equally if no skill type found" in {
    val facade = new SkillOps(skillTypeOpsMock, skillRegistryMock, loggerMock)

    skillTypeOpsMock.get(unlimited) returns None

    facade.sortHelper(unlimited, valued) shouldBe 0
  }

  "Skill.apply" should "return a SkillOps instance" in {
    val facade = Skill(skillTypeOpsMock, skillRegistryMock, loggerMock)

    facade.isInstanceOf[SkillOps] shouldBe true
  }

  it should "return a SkillOps instance without arguments" in {
    val facade = Skill()

    facade.isInstanceOf[SkillOps] shouldBe true
  }
}
