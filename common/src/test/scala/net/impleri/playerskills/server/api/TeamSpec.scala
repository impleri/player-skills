package net.impleri.playerskills.server.api

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.utils.PlayerSkillsLogger

import java.util.UUID

class TeamSpec extends BaseSpec {
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
}
