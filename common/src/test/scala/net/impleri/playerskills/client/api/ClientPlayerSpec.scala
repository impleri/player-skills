package net.impleri.playerskills.client.api

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

class ClientPlayerSpec extends BaseSpec {
  private val skillTypeOpsMock = mock[SkillTypeOps]

  private val testUnit = ClientPlayer(skillTypeOpsMock)


  "ClientPlayer.can" should "call the appropriate SkillType.can with the player's skill if both are found" in {
    val skillName = ResourceLocation("skillstest", "test_skill").get
    val givenThreshold = Option("test-value")

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenSkillType = mock[SkillType[String]]
    skillTypeOpsMock.get(givenSkill) returns Option(givenSkillType)

    val expected = false
    givenSkillType.can(givenSkill, givenThreshold) returns expected

    testUnit.can(givenSkill, givenThreshold) should be(expected)
  }

  it should "return default value if the SkillType is not found" in {
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    skillTypeOpsMock.get(givenSkill) returns None

    testUnit.can(givenSkill) should be(ClientPlayer.DEFAULT_SKILL_RESPONSE)
  }

  "ClientPlayer.apply" should "return a usable instance" in {
    val result = ClientPlayer()

    result.isInstanceOf[ClientPlayer] should be(true)
  }
}
