package net.impleri.playerskills.server.api

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

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
  private val loggerMock = mock[PlayerSkillsLogger]

  private val givenUuid = UUID.randomUUID()
  private val testUnit = new TeamOps(playerOpsMock, skillOpsMock, teamMock, loggerMock)

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

  "TeamUpdater.syncSkills" should "" in {
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
}
