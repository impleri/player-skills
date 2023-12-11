package net.impleri.playerskills.server.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import org.mockito.captor.ArgCaptor

import java.util.UUID

class PlayerSkillsCommandsSpec extends BaseSpec {
  private val skillOpsMock: SkillOps = mock[SkillOps]
  private val skillTypeOpsMock: SkillTypeOps = mock[SkillTypeOps]
  private val playerOpsMock: PlayerOps = mock[PlayerOps]
  private val teamOpsMock: TeamOps = mock[TeamOps]
  private val loggerMock: PlayerSkillsLogger = mock[PlayerSkillsLogger]

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

  "CommandHelpers.hasPermission" should "wrap hasPermission" in {
    val sourceMock = mock[CommandSourceStack]

    testUnit.hasPermission()(sourceMock)

    sourceMock.hasPermission(1) wasCalled once
  }

  it should "pass param to hasPermission" in {
    val sourceMock = mock[CommandSourceStack]

    testUnit.hasPermission(5)(sourceMock)

    sourceMock.hasPermission(5) wasCalled once
  }

  //  TODO: Get partial mock working correctly
  //  "CommandHelpers.withCurrentPlayerCommand" should "trigger the callback" in {
  //    val expected = 4
  //
  //    val contextMock = mock[CommandContext[CommandSourceStack]]
  //    val sourceMock = mock[CommandSourceStack]
  //    val playerMock = mock[Player[ServerPlayer]]
  //    val testClass = spy(testUnit)
  //
  //    testClass.getCurrentPlayer(sourceMock) returns playerMock
  //    contextMock.getSource returns sourceMock
  //    sourceMock.getPlayer returns null
  //
  //    val givenCallback = (_: Player[_]) => expected
  //
  //    val result = testClass.withCurrentPlayerCommand(givenCallback).run(contextMock)
  //
  //    result should be(expected)
  //  }

  "CommandHelpers.withSuccessMessage" should "trigger sendSuccess" in {
    val component = Component.literal("message")

    val contextMock = mock[CommandContext[CommandSourceStack]]
    val sourceMock = mock[CommandSourceStack]

    contextMock.getSource returns sourceMock

    val result = testUnit.withSuccessMessage(() => component).run(contextMock)

    sourceMock.sendSuccess(component, false) wasCalled once

    result should be(Command.SINGLE_SUCCESS)
  }

  "DebugCommands.toggleDebug" should "proxy logger.toggleDebug call" in {
    loggerMock.toggleDebug() returns true

    val response = testUnit.toggleDebug("Test label", loggerMock)()

    response.getString.contains("debug_enabled") should be(true)
  }

  it should "proxy logger.toggleDebug call with disabled message" in {
    val loggerMock = mock[PlayerSkillsLogger]

    loggerMock.toggleDebug() returns false

    val response = testUnit.toggleDebug("Test label", loggerMock)()

    response.getString.contains("debug_disabled") should be(true)
  }

  "DegradeSkillCommand.degradePlayerSkill" should "proxy teamOps.degrade" in {
    val expected = false
    val playerMock = mock[Player[_]]
    val skillName = new ResourceLocation("skillstest", "test")
    val skillMock = mock[Skill[String]]

    skillOpsMock.get[String](skillName) returns Option(skillMock)
    teamOpsMock.degrade(playerMock, skillMock, None, None) returns Option(expected)

    val received = testUnit.degradePlayerSkill[String](Option(playerMock), Option(skillName))

    teamOpsMock.degrade(playerMock, skillMock, None, None) wasCalled once

    received.value should be(expected)
  }

  it should "does nothing if no player" in {
    val skillName = new ResourceLocation("skillstest", "test")
    val skillMock = mock[Skill[String]]

    skillOpsMock.get[String](skillName) returns Option(skillMock)

    val received = testUnit.degradePlayerSkill[String](None, Option(skillName))

    teamOpsMock.degrade(*, *, *, *) wasNever called

    received should be(None)
  }

  it should "does nothing if skill not found" in {
    val playerMock = mock[Player[_]]
    val skillName = new ResourceLocation("skillstest", "test")

    skillOpsMock.get[String](skillName) returns None

    val received = testUnit.degradePlayerSkill[String](Option(playerMock), Option(skillName))

    teamOpsMock.degrade(*, *, *, *) wasNever called

    received should be(None)
  }

  it should "does nothing if no skill name given" in {
    val playerMock = mock[Player[_]]

    val received = testUnit.degradePlayerSkill(Option(playerMock), None)

    skillOpsMock.get(*) wasNever called
    teamOpsMock.degrade(*, *, *, *) wasNever called

    received should be(None)
  }

  "ImproveSkillCommand.improvePlayerSkill" should "proxy teamOps.improve" in {
    val expected = false
    val playerMock = mock[Player[_]]
    val skillName = new ResourceLocation("skillstest", "test")
    val skillMock = mock[Skill[String]]

    skillOpsMock.get[String](skillName) returns Option(skillMock)
    teamOpsMock.improve(playerMock, skillMock, None, None) returns Option(expected)

    val received = testUnit.improvePlayerSkill[String](Option(playerMock), Option(skillName))

    teamOpsMock.improve(playerMock, skillMock, None, None) wasCalled once

    received.value should be(expected)
  }

  it should "does nothing if no player" in {
    val skillName = new ResourceLocation("skillstest", "test")
    val skillMock = mock[Skill[String]]

    skillOpsMock.get[String](skillName) returns Option(skillMock)

    val received = testUnit.improvePlayerSkill[String](None, Option(skillName))

    teamOpsMock.improve(*, *, *, *) wasNever called

    received should be(None)
  }

  it should "does nothing if skill not found" in {
    val playerMock = mock[Player[_]]
    val skillName = new ResourceLocation("skillstest", "test")

    skillOpsMock.get[String](skillName) returns None

    val received = testUnit.improvePlayerSkill[String](Option(playerMock), Option(skillName))

    teamOpsMock.improve(*, *, *, *) wasNever called

    received should be(None)
  }

  it should "does nothing if no skill name given" in {
    val playerMock = mock[Player[_]]

    val received = testUnit.improvePlayerSkill(Option(playerMock), None)

    skillOpsMock.get(*) wasNever called
    teamOpsMock.improve(*, *, *, *) wasNever called

    received should be(None)
  }

  "ListAcquiredCommand.listOwnSkills" should "return acquired skills as strings" in {
    val givenUuid = UUID.randomUUID()
    val playerMock = mock[Player[_]]
    playerMock.uuid returns givenUuid

    val skill1Name = new ResourceLocation("skillstest:first")
    val skillOne = mock[Skill[_]]
    skillOne.name returns skill1Name
    playerOpsMock.can(givenUuid, skill1Name) returns false

    val skill2Name = new ResourceLocation("skillstest:name")
    val skillTwo = mock[Skill[Int]]
    skillTwo.name returns skill2Name
    skillTwo.value returns Option(42)
    playerOpsMock.can(givenUuid, skill2Name) returns true

    val skill3Name = new ResourceLocation("skillstest:other")
    val skillThree = mock[Skill[_]]
    skillThree.name returns skill3Name
    skillThree.value returns None
    playerOpsMock.can(givenUuid, skill3Name) returns true

    val skills = List(skillOne, skillTwo, skillThree)


    playerOpsMock.get(playerMock) returns skills

    val (message, received) = testUnit.listOwnSkills(playerMock)

    message.getString.contains("acquired_skills") should be(true)

    received.length should be(2)

    received.head.contains(skill2Name.toString) should be(true)
    received.head.contains("42") should be(true)

    received.last.contains(skill3Name.toString) should be(true)
    received.last.contains("None") should be(true)
  }

  it should "return message when no skills are acquired" in {
    val givenUuid = UUID.randomUUID()
    val playerMock = mock[Player[_]]
    playerMock.uuid returns givenUuid

    playerOpsMock.get(playerMock) returns List.empty

    val (message, received) = testUnit.listOwnSkills(playerMock)

    message.getString.contains("no_acquired_skills") should be(true)

    received.length should be(0)
  }

  "ListSkillsCommand.listSkills" should "return all skills as strings" in {
    val skill2Name = "skillstest:name"
    val skillTwo = mock[Skill[Int]]
    skillTwo.name returns new ResourceLocation(skill2Name)

    val skill3Name = "skillstest:other"
    val skillThree = mock[Skill[_]]
    skillThree.name returns new ResourceLocation(skill3Name)

    val skills = List(skillTwo, skillThree)

    skillOpsMock.all() returns skills

    val (message, received) = testUnit.listSkills()

    message.getString.contains("registered_skills") should be(true)

    received.length should be(2)

    received.head.contains(skill2Name) should be(true)
    received.last.contains(skill3Name) should be(true)
  }

  it should "return message when no skills are registered" in {
    skillOpsMock.all() returns List.empty

    val (message, received) = testUnit.listSkills()

    message.getString.contains("no_registered_skills") should be(true)

    received.length should be(0)
  }

  "ListTypesCommand.listTypes" should "return all skill types as strings" in {
    val skill2Name = "skillstest:name"
    val skillTwo = mock[SkillType[Int]]
    skillTwo.name returns new ResourceLocation(skill2Name)

    val skill3Name = "skillstest:other"
    val skillThree = mock[SkillType[_]]
    skillThree.name returns new ResourceLocation(skill3Name)

    val skills = List(skillTwo, skillThree)

    skillTypeOpsMock.all() returns skills

    val (message, received) = testUnit.listTypes()

    message.getString.contains("registered_types") should be(true)

    received.length should be(2)

    received.head.contains(skill2Name) should be(true)
    received.last.contains(skill3Name) should be(true)
  }

  it should "return message when no skill types are registered" in {
    skillTypeOpsMock.all() returns List.empty

    val (message, received) = testUnit.listTypes()

    message.getString.contains("no_registered_types") should be(true)

    received.length should be(0)
  }

  // TODO: switch to testing withNotification once partial mocking works as desired
  "SetCommandUtils.notifyPlayer" should "send a success message" in {
    val sourceMock = mock[CommandSourceStack]
    val playerMock = mock[Player[_]]
    val playerName = "playerName"
    playerMock.name returns playerName
    val skillName = "testskills:name"
    val success = "success"
    val failure = "failure"
    val givenResult = true

    val captor = ArgCaptor[Component]

    val received = testUnit.notifyPlayer(
      sourceMock,
      Option(playerMock),
      Option(new ResourceLocation(skillName)),
      success,
      failure,
    )(Option(givenResult))

    sourceMock.sendSuccess(captor, false) wasCalled once

    captor.value.getString.contains(success) should be(true)

    val components = captor.value.getContents.asInstanceOf[TranslatableContents].getArgs
    components.head.asInstanceOf[Component].getString.contains(skillName) should be(true)
    components.last.asInstanceOf[Option[Component]].value.getString.contains(playerName) should be(true)

    received should be(Command.SINGLE_SUCCESS)
  }

  "SetCommandUtils.notifyPlayer" should "send a failure message" in {
    val sourceMock = mock[CommandSourceStack]
    val playerMock = mock[Player[_]]
    val playerName = "playerName"
    playerMock.name returns playerName
    val skillName = "testskills:name"
    val success = "success"
    val failure = "failure"
    val givenResult = false

    val captor = ArgCaptor[Component]

    val received = testUnit.notifyPlayer(
      sourceMock,
      None,
      Option(new ResourceLocation(skillName)),
      success,
      failure,
    )(Option(givenResult))

    sourceMock.sendFailure(captor) wasCalled once

    captor.value.getString.contains(failure) should be(true)

    val components = captor.value.getContents.asInstanceOf[TranslatableContents].getArgs
    components.head.asInstanceOf[Component].getString.contains(skillName) should be(true)
    components.last.asInstanceOf[Option[Component]] should be(None)

    received should be(0)
  }

  "SetCommandUtils.notifyPlayer" should "send a failure message if no result from the callback" in {
    val sourceMock = mock[CommandSourceStack]
    val playerMock = mock[Player[_]]
    val playerName = "playerName"
    playerMock.name returns playerName
    val skillName = "testskills:name"
    val success = "success"
    val failure = "failure"

    val captor = ArgCaptor[Component]

    val received = testUnit.notifyPlayer(
      sourceMock,
      None,
      Option(new ResourceLocation(skillName)),
      success,
      failure,
    )(None)

    sourceMock.sendFailure(captor) wasCalled once

    captor.value.getString.contains("skill_not_found") should be(true)

    val components = captor.value.getContents.asInstanceOf[TranslatableContents].getArgs
    components.head.asInstanceOf[String].contains(skillName) should be(true)

    received should be(0)
  }

  "SetSkillCommand.grantPlayerSkill" should "proxy upsert for player" in {
    val sourceMock = mock[CommandSourceStack]
    val playerMock = mock[Player[_]]
    val skillName = "testskills:name"
    val skillLocation = new ResourceLocation(skillName)
    val givenValue = "nextValue"
    val parsedValue = 42
    val newSkill = mock[Skill[Int]]

    val skill = mock[Skill[Int] with ChangeableSkillOps[Int, Skill[Int]]]
    skill.mutate(Option(parsedValue)) returns newSkill

    skillOpsMock.get[Int](skillLocation) returns Option(skill)

    val skillType = mock[SkillType[Int]]
    skillType.castFromString(givenValue) returns Option(parsedValue)

    skillTypeOpsMock.get(skill) returns Option(skillType)

    playerOpsMock.upsert(playerMock, newSkill) returns List(newSkill)

    testUnit.grantPlayerSkill(sourceMock, Option(playerMock), Option(skillLocation), givenValue)

    playerOpsMock.upsert(playerMock, newSkill) wasCalled once
    sourceMock.sendSuccess(*, false) wasCalled once
  }

  "SkillValueCommand.getSkillValue" should "proxy playerOps.get" in {
    val playerMock = mock[Player[_]]
    val skillName = "testskills:name"
    val skillLocation = new ResourceLocation(skillName)

    val skill = mock[Skill[Boolean]]
    skill.name returns skillLocation
    skill.value returns Option(true)

    playerOpsMock.get[Boolean](playerMock, skillLocation) returns Option(skill)

    val (message, values) = testUnit.getSkillValue(Option(playerMock), Option(skillLocation))

    message.getString.contains("acquired_skills") should be(true)

    values.length should be(1)
    values.head.contains(skillName) should be(true)
    values.head.contains("true") should be(true)
  }

  it should "provide a different message if no skill found" in {
    val playerMock = mock[Player[_]]
    val skillName = "testskills:name"
    val skillLocation = new ResourceLocation(skillName)

    playerOpsMock.get[Boolean](playerMock, skillLocation) returns None

    val (message, values) = testUnit.getSkillValue(Option(playerMock), Option(skillLocation))

    message.getString.contains("no_acquired_skills") should be(true)

    values.length should be(0)
  }

  "SyncTeamCommands.syncTeamFor" should "proxy teamOps.syncEntireTeam" in {
    val playerMock = mock[Player[_]]

    teamOpsMock.syncEntireTeam(playerMock) returns true

    val response = testUnit.syncTeamFor(Option(playerMock))

    response should be(Command.SINGLE_SUCCESS)
  }

  it should "returns an error code if fails" in {
    val playerMock = mock[Player[_]]

    teamOpsMock.syncEntireTeam(playerMock) returns false

    val response = testUnit.syncTeamFor(Option(playerMock))

    response shouldNot be(Command.SINGLE_SUCCESS)
  }

  it should "returns an error code if player isn't found" in {
    val response = testUnit.syncTeamFor(None)

    response shouldNot be(Command.SINGLE_SUCCESS)

    teamOpsMock.syncEntireTeam(*) wasNever called
  }

  "SyncTeamCommands.syncToTeam" should "proxy teamOps.syncFromPlayer" in {
    val playerMock = mock[Player[_]]

    teamOpsMock.syncFromPlayer(playerMock) returns true

    val response = testUnit.syncToTeam(playerMock)

    response should be(Command.SINGLE_SUCCESS)
  }

  it should "returns an error code if fails" in {
    val playerMock = mock[Player[_]]

    teamOpsMock.syncFromPlayer(playerMock) returns false

    val response = testUnit.syncToTeam(playerMock)

    response shouldNot be(Command.SINGLE_SUCCESS)
  }

  "ValuesCommandUtils.withListValuesSource" should "wrap callback function to list things" in {
    val expectedMessage = Component.literal("test")
    val expectedValues = List("one", "two")
    val contextMock = mock[CommandContext[CommandSourceStack]]
    val sourceMock = mock[CommandSourceStack]

    contextMock.getSource returns sourceMock

    def callback = (_: CommandSourceStack) => (expectedMessage, expectedValues)

    val received = testUnit.withListValuesSource(callback).run(contextMock)

    sourceMock.sendSuccess(expectedMessage, false) wasCalled once
    sourceMock.sendSystemMessage(*) wasCalled twice

    received should be(Command.SINGLE_SUCCESS)
  }

  "ValuesCommandUtils.withListValues" should "wrap callback function to list things" in {
    val expectedMessage = Component.literal("test")
    val expectedValues = List.empty
    val contextMock = mock[CommandContext[CommandSourceStack]]
    val sourceMock = mock[CommandSourceStack]

    contextMock.getSource returns sourceMock

    def callback = () => (expectedMessage, expectedValues)

    val received = testUnit.withListValues(callback).run(contextMock)

    sourceMock.sendSuccess(expectedMessage, false) wasCalled once
    sourceMock.sendSystemMessage(*) wasNever called

    received should be(Command.SINGLE_SUCCESS)
  }
}
