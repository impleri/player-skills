package net.impleri.playerskills.data.conditions

import com.google.gson.JsonObject
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.utils.PlayerSkillsLogger

class SingleTargetParserSpec extends BaseSpec {
  private val mockLogger = mock[PlayerSkillsLogger]

  private case class TestConditionsBuilder() extends SingleTargetParser[String] {
    override protected def logger: PlayerSkillsLogger = mockLogger
  }

  private val testUnit = TestConditionsBuilder()

  private val mockJson = mock[JsonObject]

  "SingleTargetParser.getTarget" should "returns the target" in {
    val expectedTarget = "targetName"

    val targetJson = mock[JsonObject]
    targetJson.getAsString returns expectedTarget

    mockJson.get("target") returns targetJson

    testUnit.getTarget(mockJson).value shouldBe expectedTarget
  }

  it should "allow a custom target name" in {
    val elementName = "element"
    val expectedTarget = "targetName"

    val targetJson = mock[JsonObject]
    targetJson.getAsString returns expectedTarget

    mockJson.get(elementName) returns targetJson

    testUnit.getTarget(mockJson, elementName).value shouldBe expectedTarget
  }
}
