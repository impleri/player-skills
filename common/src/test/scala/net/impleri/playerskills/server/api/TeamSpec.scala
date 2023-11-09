package net.impleri.playerskills.server.api

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.events.handlers.EventHandlers
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

import java.util.UUID

class TeamSpec extends BaseSpec {
  private case class TestSkill(
    override val name: ResourceLocation,
    override val value: Option[String] = None,
    override val teamMode: TeamMode = TeamMode.Off(),
  ) extends Skill[String]

  private val playerOpsMock = mock[Player]
  private val skillOpsMock = mock[SkillOps]
  private val teamMock = mock[Team]
  private val eventHandlersMock = mock[EventHandlers]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val givenUuid = UUID.randomUUID()
  private val testUnit = new TeamOps(playerOpsMock, skillOpsMock, teamMock, eventHandlersMock, loggerMock)

  "StubTeam" should "always return just the given user ID" in {
    StubTeam().getTeamMembersFor(givenUuid) should be(List(givenUuid))
  }

  "TeamSkillCalculator.getSharedSkills" should "return all skills for the user that are shared with a team" in {
    val skill1 = mock[Skill[String]]
    skill1.teamMode returns TeamMode.Shared()
    val skill2 = mock[Skill[String]]
    skill2.teamMode returns TeamMode.Pyramid()
    val skill3 = mock[Skill[String]]
    skill3.teamMode returns TeamMode.Off()
    val skill4 = mock[Skill[String]]
    skill4.teamMode returns TeamMode.Shared()

    playerOpsMock.get(givenUuid) returns List(skill1, skill2, skill3, skill4)

    testUnit.getSharedSkills(givenUuid) should be(List(skill1, skill4))
  }

  "TeamSkillCalculator.getMaxTeamSkills" should "return the highest shared skills for the team" in {
    val secondUuid = UUID.randomUUID()

    val skill1Name = new ResourceLocation("testskills", "alpha")
    val skill1 = TestSkill(skill1Name, teamMode = TeamMode.Shared())
    val skill2Name = new ResourceLocation("testskills", "beta")
    val skill2 = TestSkill(skill2Name, teamMode = TeamMode.Pyramid())
    val skill3Name = new ResourceLocation("testskills", "gamma")
    val skill3 = TestSkill(skill3Name)
    val skill4Name = new ResourceLocation("testskills", "delta")
    val skill4 = TestSkill(skill4Name, teamMode = TeamMode.Shared())

    val allSkills = List(skill1, skill2, skill3, skill4)

    val players = List(givenUuid, secondUuid)

    skillOpsMock.sortHelper(*, *) answers ((a: Skill[String], b: Skill[String]) => (a.value, b.value) match {
      case (Some(_), None) => 1
      case (None, Some(_)) => -1
      case _ => 0
    })

    playerOpsMock.get[String](givenUuid, skill1Name) returns Option(skill1.copy(value = Option("alpha")))
    playerOpsMock.get[String](givenUuid, skill2Name) returns Option(skill2)
    playerOpsMock.get[String](givenUuid, skill3Name) returns Option(skill3.copy(value = Option("gamma")))
    playerOpsMock.get[String](givenUuid, skill4Name) returns Option(skill4)

    playerOpsMock.get[String](secondUuid, skill1Name) returns Option(skill1)
    playerOpsMock.get[String](secondUuid, skill2Name) returns Option(skill2.copy(value = Option("beta")))
    playerOpsMock.get[String](secondUuid, skill3Name) returns Option(skill3)
    playerOpsMock.get[String](secondUuid, skill4Name) returns Option(skill4)

    val received = testUnit.getMaxTeamSkills(players)(allSkills).map(s => (s.name, s.value))

    received should contain only (
      (skill1Name, Option("alpha")),
      (skill2Name, Option("beta")),
      (skill4Name, None),
    )
  }

  "TeamUpdater.withFullTeam" should "ensures all team members receive updates" in {
    val secondUuid = UUID.randomUUID()
    teamMock.getTeamMembersFor(givenUuid) returns List(givenUuid, secondUuid)

    val offline = List(secondUuid)
    playerOpsMock.open(List(givenUuid, secondUuid)) returns offline

    testUnit.withFullTeam(givenUuid) { team =>
      team should contain only (givenUuid, secondUuid)
      loggerMock
    }

    playerOpsMock.close(offline) was called
  }

  "TeamUpdater.updateMemberSkill" should "update the player if the player has the wrong value" in {
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = Option(14.2)
    val updatedSkill = mock[Skill[Double]]
    updatedSkill.name returns skillName
    updatedSkill.value returns skillValue

    val existingSkill = mock[Skill[Double]]

    playerOpsMock.get[Double](givenUuid, skillName) returns Option(existingSkill)

    playerOpsMock.can(givenUuid, existingSkill, skillValue) returns false

    playerOpsMock.upsert(givenUuid, updatedSkill) returns List.empty

    testUnit.updateMemberSkill(updatedSkill)(givenUuid) should be(None)
  }

  "TeamUpdater.syncSkills" should "ensure every team member has the new value" in {
    val secondUuid = UUID.randomUUID()
    val skill1Name = new ResourceLocation("testskills", "alpha")
    val skill1 = TestSkill(skill1Name, teamMode = TeamMode.Shared())
    val skill2Name = new ResourceLocation("testskills", "delta")
    val skill2 = TestSkill(skill2Name, teamMode = TeamMode.Shared())

    val allSkills = List(skill1.copy(value = Option("alpha")), skill2.copy(value = Option("delta")))

    playerOpsMock.get(givenUuid) returns allSkills
    teamMock.getTeamMembersFor(givenUuid) returns List(givenUuid, secondUuid)

    playerOpsMock.get[String](givenUuid, skill1Name) returns Option(skill1.copy(value = Option("alpha")))
    playerOpsMock.get[String](givenUuid, skill2Name) returns Option(skill2)

    playerOpsMock.get[String](secondUuid, skill1Name) returns Option(skill1)
    playerOpsMock.get[String](secondUuid, skill2Name) returns Option(skill2.copy(value = Option("delta")))

    playerOpsMock.can(givenUuid, *, Option("delta")) returns false
    playerOpsMock.can(givenUuid, *, Option("alpha")) returns true

    playerOpsMock.can(secondUuid, *, Option("alpha")) returns false
    playerOpsMock.can(secondUuid, *, Option("delta")) returns true

    playerOpsMock.upsert(givenUuid, *) returns List.empty
    playerOpsMock.upsert(secondUuid, *) returns List(
      skill1.copy(value = Option("alpha")),
      skill2,
    )

    val updated = testUnit.syncSkills(List(givenUuid, secondUuid))(allSkills)

    updated.length should be(1)

    val received = updated
      .map(v => (v._1, v._2.map(_.name).getOrElse("NAME"), v
        ._2
        .flatMap(_.value)),
      ).head

    received._1 should be(secondUuid)
    received._2 should be(skill1Name)
    received._3 should be(None)

    playerOpsMock.upsert(givenUuid, *) wasCalled once
    playerOpsMock.upsert(secondUuid, *) wasCalled once
  }

  "TeamUpdater.notifyPlayers" should "trigger notifications" in {
    val serverMock = mock[MinecraftServer]
    val playerMock = mock[MinecraftPlayer[ServerPlayer]]

    val skillName = new ResourceLocation("testskills", "alpha")
    val oldSkill = TestSkill(skillName, teamMode = TeamMode.Shared())
    val newSkill = oldSkill.copy(value = Option("newvalue"))

    val updates = List((givenUuid, Option(oldSkill)))

    serverMock.getPlayer(givenUuid) returns Option(playerMock)

    testUnit.notifyPlayers(serverMock, newSkill)(updates)

    eventHandlersMock.emitSkillChanged(playerMock, newSkill, Option(oldSkill)) wasCalled once
  }

  it should "should not trigger notifications if emit is false" in {
    val serverMock = mock[MinecraftServer]

    val skillName = new ResourceLocation("testskills", "alpha")
    val oldSkill = TestSkill(skillName, teamMode = TeamMode.Shared())
    val newSkill = oldSkill.copy(value = Option("newvalue"))

    val updates = List((givenUuid, Option(oldSkill)))

    testUnit.notifyPlayers(serverMock, newSkill, emit = false)(updates)

    serverMock.getPlayer(*) wasNever called

    eventHandlersMock.emitSkillChanged(*, *, *) wasNever called
  }

  "TeamLimit.countWith" should "count all users with the value or better" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = Option("alpha")
    val skill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Shared())

    playerOpsMock.get[String](givenUuid, skillName) returns Option(skill)
    playerOpsMock.get[String](secondUuid, skillName) returns None

    playerOpsMock.can(givenUuid, *, skillValue) returns true

    val count = testUnit.countWith(List(givenUuid, secondUuid), skill)

    count should be(1)

    playerOpsMock.can(secondUuid, *, skillValue) wasNever called
  }

  "TeamLimit.getTeamLimit" should "returns None if the team is only one player" in {
    val skillName = new ResourceLocation("testskills", "alpha")
    val skill = TestSkill(skillName, teamMode = TeamMode.Limited(5))

    testUnit.getTeamLimit(List(givenUuid), skill) should be(None)
  }

  "TeamLimit.getTeamLimit" should "returns None if the skill team mode is off" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skill = TestSkill(skillName, teamMode = TeamMode.Off())

    testUnit.getTeamLimit(List(givenUuid, secondUuid), skill) should be(None)
  }

  "TeamLimit.getTeamLimit" should "returns None if the skill team mode is shared" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skill = TestSkill(skillName, teamMode = TeamMode.Shared())

    testUnit.getTeamLimit(List(givenUuid, secondUuid), skill) should be(None)
  }

  "TeamLimit.getTeamLimit" should "returns the team mode limit otherwise" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val expectedLimit = 5
    val skill = TestSkill(skillName, teamMode = TeamMode.Limited(expectedLimit))

    testUnit.getTeamLimit(List(givenUuid, secondUuid), skill).value should be(expectedLimit)
  }

  "TeamLimit.allows" should "return true if there are fewer players than the limit allows" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val expectedLimit = 5
    val skillValue = Option("alpha")
    val skill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Limited(expectedLimit))

    playerOpsMock.get[String](givenUuid, skillName) returns Option(skill)
    playerOpsMock.get[String](secondUuid, skillName) returns None

    playerOpsMock.can(givenUuid, *, skillValue) returns true

    testUnit.allows(List(givenUuid, secondUuid), skill) should be(true)
  }

  "TeamLimit.allows" should "return true if there is no limit" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = Option("alpha")
    val skill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Shared())

    playerOpsMock.get[String](givenUuid, skillName) returns Option(skill)
    playerOpsMock.get[String](secondUuid, skillName) returns None

    playerOpsMock.can(givenUuid, *, skillValue) returns true

    testUnit.allows(List(givenUuid, secondUuid), skill) should be(true)
  }

  "TeamLimit.allows" should "return false if there are more players than the limit allows" in {
    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val expectedLimit = 1
    val skillValue = Option("alpha")
    val skill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Limited(expectedLimit))

    playerOpsMock.get[String](givenUuid, skillName) returns Option(skill)
    playerOpsMock.get[String](secondUuid, skillName) returns None

    playerOpsMock.can(givenUuid, *, skillValue) returns true

    testUnit.allows(List(givenUuid, secondUuid), skill) should be(false)
  }

  "TeamOps.degrade" should "updates shared skill to a lower value" in {
    val serverMock = mock[MinecraftServer]
    val playerMock = mock[MinecraftPlayer[ServerPlayer]]

    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = Option("oldvalue")
    val newValue = None
    val oldSkill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Shared())
    val newSkill = oldSkill.copy(value = newValue)

    val offline = List(secondUuid)

    playerMock.uuid returns givenUuid
    playerMock.server returns serverMock

    serverMock.getPlayer(givenUuid) returns Option(playerMock)

    teamMock.getTeamMembersFor(givenUuid) returns List(givenUuid, secondUuid)

    skillOpsMock.calculatePrev(oldSkill) returns newValue

    playerOpsMock.open(List(givenUuid, secondUuid)) returns offline
    playerOpsMock.get[String](givenUuid, skillName) returns Option(oldSkill)
    playerOpsMock.get[String](secondUuid, skillName) returns None
    playerOpsMock.can(givenUuid, oldSkill, newValue) returns false
    playerOpsMock.can(secondUuid, *, newValue) returns true
    playerOpsMock.upsert(givenUuid, *) returns List(newSkill)
    playerOpsMock.calculateValue(givenUuid, oldSkill, newValue) returns Option(newSkill)

    testUnit.degrade(playerMock, oldSkill)

    eventHandlersMock.emitSkillChanged(playerMock, newSkill, Option(oldSkill)) wasCalled once
  }

  "TeamOps.improve" should "updates shared skill to a higher value" in {
    val serverMock = mock[MinecraftServer]
    val playerMock = mock[MinecraftPlayer[ServerPlayer]]

    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = None
    val newValue = Option("newvalue")
    val oldSkill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Off())
    val newSkill = oldSkill.copy(value = newValue)

    val offline = List(secondUuid)

    playerMock.uuid returns givenUuid
    playerMock.server returns serverMock

    serverMock.getPlayer(givenUuid) returns Option(playerMock)

    teamMock.getTeamMembersFor(givenUuid) returns List(givenUuid, secondUuid)

    skillOpsMock.calculateNext(oldSkill) returns newValue

    playerOpsMock.open(List(givenUuid, secondUuid)) returns offline
    playerOpsMock.get[String](givenUuid, skillName) returns Option(oldSkill)
    playerOpsMock.get[String](secondUuid, skillName) returns None
    playerOpsMock.can(givenUuid, oldSkill, newValue) returns false
    playerOpsMock.upsert(givenUuid, *) returns List(newSkill)
    playerOpsMock.calculateValue(givenUuid, oldSkill, newValue) returns Option(newSkill)

    testUnit.improve(playerMock, oldSkill)

    eventHandlersMock.emitSkillChanged(playerMock, newSkill, Option(oldSkill)) wasCalled once
  }

  it should "does nothing if there is no update" in {
    val serverMock = mock[MinecraftServer]
    val playerMock = mock[MinecraftPlayer[ServerPlayer]]

    val secondUuid = UUID.randomUUID()
    val skillName = new ResourceLocation("testskills", "alpha")
    val skillValue = None
    val newValue = Option("newvalue")
    val oldSkill = TestSkill(skillName, value = skillValue, teamMode = TeamMode.Off())
    val newSkill = oldSkill.copy(value = newValue)

    val offline = List(secondUuid)

    playerMock.uuid returns givenUuid
    playerMock.server returns serverMock

    serverMock.getPlayer(givenUuid) returns Option(playerMock)

    teamMock.getTeamMembersFor(givenUuid) returns List(givenUuid, secondUuid)

    skillOpsMock.calculateNext(oldSkill) returns newValue

    playerOpsMock.open(List(givenUuid, secondUuid)) returns offline
    playerOpsMock.get[String](givenUuid, skillName) returns Option(oldSkill)
    playerOpsMock.can(givenUuid, oldSkill, newValue) returns false
    playerOpsMock.calculateValue(givenUuid, oldSkill, newValue) returns None

    testUnit.improve(playerMock, oldSkill)

    eventHandlersMock.emitSkillChanged(*, *, *) wasNever called
  }
}
