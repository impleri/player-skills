package net.impleri.playerskills.server.commands

import com.mojang.brigadier.CommandDispatcher
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.commands.CommandSourceStack

import java.util.UUID

class PlayerSkillsCommandsSpec extends BaseSpec {
  private val skillOpsMock: SkillOps = mock[SkillOps]
  private val skillTypeOpsMock: SkillTypeOps = mock[SkillTypeOps]
  private val playerOpsMock: PlayerOps = mock[PlayerOps]
  private val teamOpsMock: TeamOps = mock[TeamOps]
  private val loggerMock: Logger = mock[Logger]

  private val testUnit: PlayerSkillsCommands = new PlayerSkillsCommands(
    skillOpsMock,
    skillTypeOpsMock,
    playerOpsMock,
    teamOpsMock,
    loggerMock,
    loggerMock,
    loggerMock,
    loggerMock,
    loggerMock,
  )

  "PlayerSkillsCommands.register" should "register all of the commands" in {
    val dispatcher = mock[CommandDispatcher[CommandSourceStack]]
    testUnit.register(dispatcher)

    dispatcher.register(*) wasCalled once
  }

  "DebugCommands.toggleDebug" should "proxy logger.toggleDebug call" in {
    loggerMock.toggleDebug() returns true

    val response = testUnit.toggleDebug("Test label", loggerMock)

    response.value.asString.contains("debug_enabled") should be(true)
  }

  it should "proxy logger.toggleDebug call with disabled message" in {
    val loggerMock = mock[Logger]

    loggerMock.toggleDebug() returns false

    val response = testUnit.toggleDebug("Test label", loggerMock)

    response.value.asString.contains("debug_disabled") should be(true)
  }

  "ListAcquiredCommand.listOwnSkills" should "return acquired skills as strings" in {
    val givenUuid = UUID.randomUUID()
    val playerMock = mock[Player]
    playerMock.uuid returns givenUuid

    val skill1Name = ResourceLocation("skillstest:first").get
    val skillOne = mock[Skill[_]]
    skillOne.name returns skill1Name
    playerOpsMock.can(givenUuid, skill1Name) returns false

    val skill2Name = ResourceLocation("skillstest:name").get
    val skillTwo = mock[Skill[Int]]
    skillTwo.name returns skill2Name
    skillTwo.value returns Option(42)
    playerOpsMock.can(givenUuid, skill2Name) returns true

    val skill3Name = ResourceLocation("skillstest:other").get
    val skillThree = mock[Skill[_]]
    skillThree.name returns skill3Name
    skillThree.value returns None
    playerOpsMock.can(givenUuid, skill3Name) returns true

    val skills = List(skillOne, skillTwo, skillThree)


    playerOpsMock.get(playerMock) returns skills

    val message = testUnit.getPlayerSkills(playerMock)

    message.output.getString.contains("acquired_skills") should be(true)

    message.children.length should be(2)

    message.children.head.output.getString.contains(skill2Name.toString) should be(true)
    message.children.head.output.getString.contains("42") should be(true)

    message.children.last.output.getString.contains(skill3Name.toString) should be(true)
    message.children.last.output.getString.contains("None") should be(true)
  }

  it should "return message when no skills are acquired" in {
    val givenUuid = UUID.randomUUID()
    val playerMock = mock[Player]
    playerMock.uuid returns givenUuid

    playerOpsMock.get(playerMock) returns List.empty

    val message = testUnit.getPlayerSkills(playerMock)

    message.output.getString.contains("no_acquired_skills") should be(true)

    message.children.length should be(0)
  }
}
