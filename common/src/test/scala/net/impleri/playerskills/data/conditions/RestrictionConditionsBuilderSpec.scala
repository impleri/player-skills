package net.impleri.playerskills.data.conditions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.{ResourceLocation => ResourceFacade}
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import java.util.{List => JavaList}
import java.util.UUID

class RestrictionConditionsBuilderSpec extends BaseSpec {
  private val targetName = new ResourceLocation("skillstest", "condition")
  private val mockSkillOps = mock[SkillOps]
  private val mockSkillTypeOps = mock[SkillTypeOps]
  private val mockPlayerOps = mock[PlayerOps]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val mockParse = mock[JsonObject => Unit]
  private val mockToggle = mock[() => Unit]

  private case class TestConditionsBuilder(
    override val name: ResourceLocation = targetName,
    override val skillOps: SkillOps = mockSkillOps,
    override val skillTypeOps: SkillTypeOps = mockSkillTypeOps,
    override val playerOps: PlayerOps = mockPlayerOps,
    override val logger: PlayerSkillsLogger = mockLogger,
  ) extends RestrictionConditionsBuilder {
    override def parseRestriction(jsonElement: JsonObject): Unit = {
      mockParse(jsonElement)
    }

    override def toggleEverything(): Unit = mockToggle()

    override def toggleNothing(): Unit = mockToggle()
  }

  private val testUnit = TestConditionsBuilder()

  private val mockJson = mock[JsonObject]

  "RestrictionConditionsBuilder.parse" should "parses the condition" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceFacade(expectedSkillName, isSkill = false).get
    val expectedValue = "value"

    val conditionSkillJson = mock[JsonPrimitive]
    conditionSkillJson.isJsonPrimitive returns true
    conditionSkillJson.isString returns true
    conditionSkillJson.getAsString returns expectedSkillName

    val actionJson = mock[JsonPrimitive]
    actionJson.isJsonPrimitive returns true
    actionJson.isString returns true
    actionJson.getAsString returns "cannot"

    val valueJson = mock[JsonElement]
    valueJson.isJsonPrimitive returns true
    valueJson.getAsString returns expectedValue

    val conditionFnJson = mock[JsonObject]
    conditionFnJson.get("action") returns actionJson
    conditionFnJson.get("skill") returns conditionSkillJson
    conditionFnJson.get("value") returns valueJson

    val conditionJson = mock[JsonElement]
    conditionJson.getAsJsonObject returns conditionFnJson

    val arrayJson = mock[JsonArray]
    arrayJson.iterator returns JavaList.of(conditionJson).iterator

    val conditionsJson = mock[JsonElement]
    conditionsJson.isJsonArray returns true
    conditionsJson.getAsJsonArray returns arrayJson

    mockJson.get(*) answers ((el: String) => if (el == "if") conditionsJson else new JsonNull())

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    val mockType = mock[SkillType[String]]
    mockType.castFromString(expectedValue) returns Option(expectedValue)

    mockSkillTypeOps.get(mockSkill) returns Option(mockType)

    testUnit.parse(mockJson)

    val mockPlayer = mock[Player[_]]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, Option(expectedValue)) returns true

    testUnit.condition(mockPlayer) shouldBe false
  }

  it should "parses the unless condition" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceFacade(expectedSkillName, isSkill = false).get
    val expectedValue = "value"

    val conditionSkillJson = mock[JsonPrimitive]
    conditionSkillJson.isJsonPrimitive returns true
    conditionSkillJson.isString returns true
    conditionSkillJson.getAsString returns expectedSkillName

    val actionJson = mock[JsonPrimitive]
    actionJson.isJsonPrimitive returns true
    actionJson.isString returns true
    actionJson.getAsString returns "can"

    val valueJson = mock[JsonElement]
    valueJson.isJsonPrimitive returns true
    valueJson.getAsString returns expectedValue

    val conditionFnJson = mock[JsonObject]
    conditionFnJson.get("action") returns actionJson
    conditionFnJson.get("skill") returns conditionSkillJson
    conditionFnJson.get("value") returns valueJson

    val conditionJson = mock[JsonElement]
    conditionJson.getAsJsonObject returns conditionFnJson

    val arrayJson = mock[JsonArray]
    arrayJson.iterator returns JavaList.of(conditionJson).iterator

    val conditionsJson = mock[JsonElement]
    conditionsJson.isJsonArray returns true
    conditionsJson.getAsJsonArray returns arrayJson

    mockJson.get(*) answers ((el: String) => if (el == "unless") conditionsJson else new JsonNull())

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    val mockType = mock[SkillType[String]]
    mockType.castFromString(expectedValue) returns Option(expectedValue)

    mockSkillTypeOps.get(mockSkill) returns Option(mockType)

    testUnit.parse(mockJson)

    val mockPlayer = mock[Player[_]]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, Option(expectedValue)) returns false

    testUnit.condition(mockPlayer) shouldBe true
  }

  it should "handle no skill value" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceFacade(expectedSkillName, isSkill = false).get
    val expectedValue = ""

    val conditionSkillJson = mock[JsonPrimitive]
    conditionSkillJson.isJsonPrimitive returns true
    conditionSkillJson.isString returns true
    conditionSkillJson.getAsString returns expectedSkillName

    val actionJson = mock[JsonPrimitive]
    actionJson.isJsonPrimitive returns true
    actionJson.isString returns true
    actionJson.getAsString returns "can"

    val valueJson = mock[JsonElement]
    valueJson.isJsonPrimitive returns true
    valueJson.getAsString returns expectedValue

    val conditionFnJson = mock[JsonObject]
    conditionFnJson.get("action") returns actionJson
    conditionFnJson.get("skill") returns conditionSkillJson
    conditionFnJson.get("value") returns valueJson

    val conditionJson = mock[JsonElement]
    conditionJson.getAsJsonObject returns conditionFnJson

    val arrayJson = mock[JsonArray]
    arrayJson.iterator returns JavaList.of(conditionJson).iterator

    val conditionsJson = mock[JsonElement]
    conditionsJson.isJsonArray returns true
    conditionsJson.getAsJsonArray returns arrayJson

    mockJson.get(*) answers ((el: String) => if (el == "if") conditionsJson else new JsonNull())

    val mockSkill = mock[Skill[String]]
    mockSkill.name returns expectedSkill
    mockSkillOps.get[String](expectedSkill) returns Option(mockSkill)

    mockSkillTypeOps.get(mockSkill) returns None

    testUnit.parse(mockJson)

    val mockPlayer = mock[Player[_]]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockPlayerOps.can(mockUuid, expectedSkill, None) returns true

    testUnit.condition(mockPlayer) shouldBe true
  }

  it should "return false if no skill found" in {
    val expectedSkillName = "skillstest:condition"
    val expectedSkill = ResourceFacade(expectedSkillName, isSkill = false).get
    val expectedValue = ""

    val conditionSkillJson = mock[JsonPrimitive]
    conditionSkillJson.isJsonPrimitive returns true
    conditionSkillJson.isString returns true
    conditionSkillJson.getAsString returns expectedSkillName

    val actionJson = mock[JsonPrimitive]
    actionJson.isJsonPrimitive returns true
    actionJson.isString returns true
    actionJson.getAsString returns "can"

    val valueJson = mock[JsonElement]
    valueJson.isJsonPrimitive returns true
    valueJson.getAsString returns expectedValue

    val conditionFnJson = mock[JsonObject]
    conditionFnJson.get("action") returns actionJson
    conditionFnJson.get("skill") returns conditionSkillJson
    conditionFnJson.get("value") returns valueJson

    val conditionJson = mock[JsonElement]
    conditionJson.getAsJsonObject returns conditionFnJson

    val arrayJson = mock[JsonArray]
    arrayJson.iterator returns JavaList.of(conditionJson).iterator

    val conditionsJson = mock[JsonElement]
    conditionsJson.isJsonArray returns true
    conditionsJson.getAsJsonArray returns arrayJson

    mockJson.get(*) answers ((el: String) => if (el == "unless") conditionsJson else new JsonNull())

    mockSkillOps.get[String](expectedSkill) returns None

    testUnit.parse(mockJson)

    val mockPlayer = mock[Player[_]]
    val mockUuid = UUID.randomUUID()
    mockPlayer.uuid returns mockUuid

    mockSkillTypeOps.get(*[Skill[_]]) wasNever called

    testUnit.condition(mockPlayer) shouldBe false

    mockPlayerOps.can(*, *, *) wasNever called
  }

  it should "toggle everything" in {
    val toggleJson = mock[JsonPrimitive]
    toggleJson.isJsonPrimitive returns true
    toggleJson.isBoolean returns true
    toggleJson.getAsBoolean returns true

    mockJson.get(*) answers ((el: String) => if (el == "everything") toggleJson else new JsonNull())

    testUnit.parse(mockJson)

    mockToggle() wasCalled once
  }

  it should "toggle nothing" in {
    val toggleJson = mock[JsonPrimitive]
    toggleJson.isJsonPrimitive returns true
    toggleJson.isBoolean returns true
    toggleJson.getAsBoolean returns true

    mockJson.get(*) answers ((el: String) => if (el == "nothing") toggleJson else new JsonNull())

    testUnit.parse(mockJson)

    mockToggle() wasCalled once
  }
}
