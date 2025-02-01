package net.impleri.playerskills.data.conditions

import com.google.gson.{JsonObject, JsonParser}
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation

import java.util.UUID

class RestrictionConditionsBuilderSpec extends BaseSpec {
  private val targetName = ResourceLocation("skillstest", "condition").get
  private val mockSkillOps = mock[SkillOps]
  private val mockSkillTypeOps = mock[SkillTypeOps]
  private val mockPlayerOps = mock[PlayerOps]
  private val mockLogger = mock[Logger]

  private val mockParse = mock[JsonObject => Unit]
  private val mockToggle = mock[() => Unit]

  private case class TestConditionsBuilder(
    override val name: ResourceLocation = targetName,
    override val skillOps: SkillOps = mockSkillOps,
    override val skillTypeOps: SkillTypeOps = mockSkillTypeOps,
    override val playerOps: PlayerOps = mockPlayerOps,
    override val logger: Logger = mockLogger,
  ) extends RestrictionConditionsBuilder with SingleTargetParser[String] {
    override def isValid: Boolean = true

    override def getTarget: String = "target"

    override def parseRestriction(jsonElement: JsonObject): Unit = {
      mockParse(jsonElement)

    }

    override def toggleEverything(): Unit = mockToggle()

    override def toggleNothing(): Unit = mockToggle()
  }

  private val testUnit = TestConditionsBuilder()

  "RestrictionConditionsBuilder.parse" should "parses the condition" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceLocation(expectedSkillName).get
    val expectedValue = "value"
    val jsonString =
      s"""
        | {
        |   "if": {
        |     "action": "cannot",
        |     "skill": "$expectedSkillName",
        |     "value": "$expectedValue"
        |   }
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    val mockType = mock[SkillType[String]]
    mockType.castFromString(expectedValue) returns Option(expectedValue)

    mockSkillTypeOps.get(mockSkill) returns Option(mockType)

    testUnit.parse(json)

    val mockPlayer = mock[Player]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, Option(expectedValue)) returns true

    testUnit.condition(mockPlayer) shouldBe false
  }

  it should "parses the unless condition" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceLocation(expectedSkillName).get
    val expectedValue = "value"
    val jsonString =
      s"""
        | {
        |   "unless": {
        |     "action": "can",
        |     "skill": "$expectedSkillName",
        |     "value": "$expectedValue"
        |   }
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    val mockType = mock[SkillType[String]]
    mockType.castFromString(expectedValue) returns Option(expectedValue)

    mockSkillTypeOps.get(mockSkill) returns Option(mockType)

    testUnit.parse(json)

    val mockPlayer = mock[Player]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, Option(expectedValue)) returns false

    testUnit.condition(mockPlayer) shouldBe true
  }

  it should "handle no skill value" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceLocation(expectedSkillName).get
    val expectedValue = ""
    val jsonString =
      s"""
        | {
        |   "if": [{
        |     "action": "can",
        |     "skill": "$expectedSkillName",
        |     "value": "$expectedValue"
        |   }]
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    mockSkillTypeOps.get(mockSkill) returns None

    testUnit.parse(json)

    val mockPlayer = mock[Player]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, None) returns true

    testUnit.condition(mockPlayer) shouldBe true
  }

  it should "return false if no skill found" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceLocation(expectedSkillName).get
    val expectedValue = ""
    val jsonString =
      s"""
        | {
        |   "if": [{
        |     "action": "can",
        |     "skill": "$expectedSkillName",
        |     "value": "$expectedValue"
        |   }]
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    mockSkillOps.get[String](expectedSkill) returns None

    testUnit.parse(json)

    val mockPlayer = mock[Player]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockSkillTypeOps.get(*[Skill[_]]) wasNever called

    testUnit.condition(mockPlayer) shouldBe false

    mockPlayerOps.can(*, *, *) wasNever called
  }

  it should "toggle everything" in {
    val jsonString =
      s"""
        | {
        |   "everything": true
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    testUnit.parse(json)

    mockToggle() wasCalled once
  }

  it should "toggle nothing" in {
    val jsonString =
      s"""
        | {
        |   "nothing": true
        | }
        |""".stripMargin
    val json = JsonParser.parseString(jsonString).getAsJsonObject

    testUnit.parse(json)

    mockToggle() wasCalled once
  }
}
