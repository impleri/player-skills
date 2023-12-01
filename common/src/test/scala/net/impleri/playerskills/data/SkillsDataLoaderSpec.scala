package net.impleri.playerskills.data

import com.google.gson.JsonElement
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.skills.basic.BasicSkill
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller

import scala.jdk.javaapi.CollectionConverters

class SkillsDataLoaderSpec extends BaseSpec {
  private val resourceManagerMock = mock[ResourceManager]
  private val profileFillerMock = mock[ProfilerFiller]
  private val skillOpsMock = mock[SkillOps]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUnit = SkillsDataLoader(skillOpsMock, loggerMock)

  private val skillDescription = "Description of the skill"
  private val notifyMessage = "notify_message"
  private val changesAllowed = 4
  private val skillName = new ResourceLocation("skillstest:skill_name")

  private val minimalSkillJson =
    s"""
       | {
       |   "type": "basic"
       | }
       |""".stripMargin
  private val minimalSkill = BasicSkill(
    skillName,
  )

  private val basicSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "description": "$skillDescription",
       |   "changesAllowed": $changesAllowed,
       |   "notify": "$notifyMessage",
       |   "teamMode": "shared"
       | }
       |""".stripMargin
  private val basicSkill = BasicSkill(
    skillName,
    description = Option(skillDescription),
    changesAllowed = changesAllowed,
    notifyKey = Option(notifyMessage),
    announceChange = true,
    teamMode = TeamMode.Shared(),
  )

  private val numericSkillJson =
    s"""
       | {
       |   "type": "numeric",
       |   "notify": true,
       |   "initialValue": 1.0,
       |   "step": 0.25,
       |   "teamMode": {
       |     "mode": "proportional",
       |     "rate": 33.3
       |   }
       | }
       |""".stripMargin
  private val numericSkill = NumericSkill(
    skillName,
    value = Option(1.0),
    announceChange = true,
    step = 0.25,
    teamMode = TeamMode.Proportional(33.3),
  )

  private val tieredSkillJson =
    s"""
       | {
       |   "type": "tiered",
       |   "options": ["alpha", "beta", "gamma", "delta"],
       |   "teamMode": "pyramid"
       | }
       |""".stripMargin
  private val tieredSkill = TieredSkill(
    skillName,
    options = List("alpha", "beta", "gamma", "delta"),
    teamMode = TeamMode.Pyramid(),
  )

  private val specializedSkillJson =
    s"""
       | {
       |   "type": "specialized",
       |   "teamMode": "splitEvenly"
       | }
       |""".stripMargin
  private val specializedSkill = SpecializedSkill(
    skillName,
    teamMode = TeamMode.SplitEvenly(),
  )

  private val limitedSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "teamMode": {
       |     "mode": "limited",
       |     "rate": 10
       |   }
       | }
       |""".stripMargin
  private val limitedSkill = BasicSkill(
    skillName,
    teamMode = TeamMode.Limited(10),
  )

  private val wrongTeamSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "teamMode": "broken"
       | }
       |""".stripMargin
  private val wrongTeamSkill = BasicSkill(
    skillName,
  )

  private val badTeamSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "teamMode": 15"
       | }
       |""".stripMargin
  private val badTeamSkill = BasicSkill(
    skillName,
  )

  private val notPyramidTeamSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "teamMode": "pyramid"
       | }
       |""".stripMargin
  private val notPyramidTeamSkill = BasicSkill(
    skillName,
  )

  private val notSplitEvenlyTeamSkillJson =
    s"""
       | {
       |   "type": "basic",
       |   "teamMode": "splitEvenly"
       | }
       |""".stripMargin
  private val notSplitEvenlyTeamSkill = BasicSkill(
    skillName,
  )

  private val unknownTeamSkillJson =
    s"""
       | {
       |   "type": "unknown"
       | }
       |""".stripMargin

  private def testLoadData(json: String, expected: Skill[_]): Unit = {
    val jsonElement = SkillsDataLoader.GsonService.fromJson(json, classOf[JsonElement])
    val input = CollectionConverters.asJava(Map(skillName -> jsonElement))

    testUnit.apply(input, resourceManagerMock, profileFillerMock)

    skillOpsMock.upsert(expected) wasCalled once
  }

  "SkillsDataLoader.apply" should "create a minimal BasicSkill" in {
    testLoadData(minimalSkillJson, minimalSkill)
  }

  "SkillsDataLoader.apply" should "create a BasicSkill" in {
    testLoadData(basicSkillJson, basicSkill)
  }

  "SkillsDataLoader.apply" should "create a NumericSkill" in {
    testLoadData(numericSkillJson, numericSkill)
  }

  "SkillsDataLoader.apply" should "create a TieredSkill" in {
    testLoadData(tieredSkillJson, tieredSkill)
  }

  "SkillsDataLoader.apply" should "create a SpecializedSkill" in {
    testLoadData(specializedSkillJson, specializedSkill)
  }

  "SkillsDataLoader.apply" should "create a team limited skill" in {
    testLoadData(limitedSkillJson, limitedSkill)
  }

  "SkillsDataLoader.apply" should "ignore unknown team modes" in {
    testLoadData(wrongTeamSkillJson, wrongTeamSkill)
  }

  "SkillsDataLoader.apply" should "ignore unexpected team mode types" in {
    testLoadData(badTeamSkillJson, badTeamSkill)
  }

  "SkillsDataLoader.apply" should "ignore disallowed pyramid team mode" in {
    testLoadData(notPyramidTeamSkillJson, notPyramidTeamSkill)
  }

  "SkillsDataLoader.apply" should "ignore disallowed splitEvenly team mode" in {
    testLoadData(notSplitEvenlyTeamSkillJson, notSplitEvenlyTeamSkill)
  }

  "SkillsDataLoader.apply" should "ignore unknown skill types" in {
    val jsonElement = SkillsDataLoader.GsonService.fromJson(unknownTeamSkillJson, classOf[JsonElement])
    val input = CollectionConverters.asJava(Map(skillName -> jsonElement))

    testUnit.apply(input, resourceManagerMock, profileFillerMock)

    skillOpsMock.upsert(*) wasNever called
  }
}
