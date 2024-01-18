package net.impleri.playerskills.server.api

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.server.skills.PlayerRegistry

import java.util.UUID

class PlayerSpec extends BaseSpec {
  private val registryMock = mock[PlayerRegistry]
  private val skillTypeOpsMock = mock[SkillTypeOps]
  private val skillOpsMock = mock[SkillOps]
  private val playerMock = mock[MinecraftPlayer[_]]

  private val testUnit = new Player(registryMock, skillTypeOpsMock, skillOpsMock)

  "PlayerRegistryFacade.get" should "proxy PlayerRegistry.get" in {
    val givenUuid = UUID.randomUUID()
    val expected = List.empty[Skill[_]]

    registryMock.get(givenUuid) returns expected

    testUnit.get(givenUuid) should be(expected)
  }

  it should "proxy PlayerRegistry.get with a player" in {
    val givenUuid = UUID.randomUUID()
    playerMock.uuid returns givenUuid
    val expected = List.empty[Skill[_]]

    registryMock.get(givenUuid) returns expected

    testUnit.get(playerMock) should be(expected)
  }

  it should "return a skill matching the given name" in {
    val givenUuid = UUID.randomUUID()
    val givenName = ResourceLocation("skillstest", "test_skill").get
    val expected = mock[Skill[_]]
    expected.name returns givenName

    val returnedList = List(expected)

    registryMock.get(givenUuid) returns returnedList

    testUnit.get(givenUuid, givenName).value should be(expected)
  }

  it should "return a skill matching the given player name" in {
    val givenUuid = UUID.randomUUID()
    val givenName = ResourceLocation("skillstest", "test_skill").get
    val expected = mock[Skill[_]]
    expected.name returns givenName

    playerMock.uuid returns givenUuid

    val returnedList = List(expected)

    registryMock.get(givenUuid) returns returnedList

    testUnit.get(playerMock, givenName).value should be(expected)
  }

  it should "return None if it cannot find a skill matching the given name" in {
    val givenUuid = UUID.randomUUID()
    val givenName = ResourceLocation("skillstest", "test_skill").get
    val expected = mock[Skill[_]]

    val returnedList = List(expected)

    registryMock.get(givenUuid) returns returnedList

    testUnit.get(givenUuid, givenName) should be(None)
  }

  "PlayerRegistryFacade.isOnline" should "proxy PlayerRegistry.has" in {
    val givenUuid = UUID.randomUUID()
    val expected = false

    registryMock.has(givenUuid) returns expected

    testUnit.isOnline(givenUuid) should be(expected)
  }

  "PlayerRegistryFacade.open" should "proxy PlayerRegistry.open" in {
    val givenUuid = UUID.randomUUID()
    val expected = List.empty[Skill[_]]

    registryMock.open(givenUuid) returns expected

    testUnit.open(givenUuid) should be(expected)
  }

  it should "proxy PlayerRegistry.open with a list" in {
    val givenUuid = UUID.randomUUID()
    val givenList = List(givenUuid, UUID.randomUUID())
    val expected = List(givenUuid)

    registryMock.open(givenList) returns expected

    testUnit.open(givenList) should be(expected)
  }

  "PlayerRegistryFacade.upsert" should "proxy PlayerRegistry.upsert" in {
    val givenUuid = UUID.randomUUID()
    val givenSkill = mock[Skill[_]]
    val expected = List(givenSkill)

    registryMock.upsert(givenUuid, givenSkill) returns expected

    testUnit.upsert(givenUuid, givenSkill) should be(expected)
  }

  it should "proxy PlayerRegistry.upsert with a player" in {
    val givenUuid = UUID.randomUUID()
    val givenSkill = mock[Skill[_]]
    val expected = List(givenSkill)

    playerMock.uuid returns givenUuid

    registryMock.upsert(givenUuid, givenSkill) returns expected

    testUnit.upsert(playerMock, givenSkill) should be(expected)
  }

  "PlayerRegistryFacade.close" should "proxy PlayerRegistry.close" in {
    val givenUuid = UUID.randomUUID()
    val expected = false

    registryMock.close(givenUuid) returns expected

    testUnit.close(givenUuid) should be(expected)
  }

  it should "proxy PlayerRegistry.close with a list" in {
    val givenUuid = UUID.randomUUID()
    val givenList = List(givenUuid, UUID.randomUUID())
    val returnedList = List(givenUuid)

    registryMock.close(givenList) returns returnedList

    testUnit.close(givenList) should be(true)
  }

  "Player.can" should "call the appropriate SkillType.can with the player's skill if both are found" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get
    val givenThreshold = Option("test-value")

    val givenSkillType = mock[SkillType[String]]
    skillTypeOpsMock.get[String](skillName) returns Option(givenSkillType)

    val otherSkill = mock[Skill[String]]
    otherSkill.name returns ResourceLocation("skillstest", "other_skill").get
    val foundSkill = mock[Skill[String]]
    foundSkill.name returns skillName
    val foundSkills = List(otherSkill, foundSkill)
    registryMock.get(givenUuid) returns foundSkills

    val expected = false
    givenSkillType.can(foundSkill, givenThreshold) returns expected

    testUnit.can(givenUuid, skillName, givenThreshold) should be(expected)
  }

  it should "return default value if the SkillType is not found" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    skillTypeOpsMock.get(givenSkill) returns None

    val otherSkill = mock[Skill[String]]
    otherSkill.name returns ResourceLocation("skillstest", "other_skill").get
    val foundSkill = mock[Skill[String]]
    foundSkill.name returns skillName
    val foundSkills = List(otherSkill, foundSkill)
    registryMock.get(givenUuid) returns foundSkills

    testUnit.can(givenUuid, skillName) should be(Player.DEFAULT_SKILL_RESPONSE)
  }

  it should "return default value if the skill is not found for the player" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenSkillType = mock[SkillType[String]]
    skillTypeOpsMock.get(givenSkill) returns Option(givenSkillType)

    val otherSkill = mock[Skill[String]]
    otherSkill.name returns ResourceLocation("skillstest", "other_skill").get

    val foundSkills = List(otherSkill)
    registryMock.get(givenUuid) returns foundSkills

    testUnit.can(givenUuid, skillName) should be(Player.DEFAULT_SKILL_RESPONSE)

    givenSkillType.can(*, None) wasNever called
  }

  "Player.reset" should "upsert player with the default skill value" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val foundSkill = mock[Skill[String]]
    skillOpsMock.get[String](skillName) returns Option(foundSkill)

    val expected = List(foundSkill)
    registryMock.upsert(givenUuid, foundSkill) returns expected

    testUnit.reset(givenUuid, givenSkill) should be(expected)
  }

  it should "do nothing if the default skill value cannot be found" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    skillOpsMock.get[String](skillName) returns None

    playerMock.uuid returns givenUuid

    testUnit.reset(playerMock, givenSkill) should be(List.empty)

    registryMock.upsert(givenUuid, *) wasNever called
  }

  "Player.calculateValue" should "return a new valid value" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenValue = Option("test-value")

    val foundSkill = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]
    foundSkill.name returns skillName
    foundSkill.areChangesAllowed() returns true
    foundSkill.isAllowedValue(givenValue) returns true
    foundSkill.value returns None

    val foundSkills = List(foundSkill)
    registryMock.get(givenUuid) returns foundSkills

    val createdSkill = mock[Skill[String]]
    foundSkill.mutate(givenValue) returns createdSkill

    testUnit.calculateValue(givenUuid, givenSkill, givenValue).value should be(createdSkill)

    skillOpsMock.get(skillName) wasNever called
  }

  it should "return a new valid value using the default skill" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenValue = Option("test-value")

    val foundSkill = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]
    foundSkill.name returns skillName
    foundSkill.areChangesAllowed() returns true
    foundSkill.isAllowedValue(givenValue) returns true
    foundSkill.value returns None

    registryMock.get(givenUuid) returns List.empty
    skillOpsMock.get[String](skillName) returns Option(foundSkill)

    val createdSkill = mock[Skill[String]]
    foundSkill.mutate(givenValue) returns createdSkill

    playerMock.uuid returns givenUuid

    testUnit.calculateValue(playerMock, givenSkill, givenValue).value should be(createdSkill)
  }

  it should "returns None if the skill cannot be changed" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenValue = Option("test-value")

    val foundSkill = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]
    foundSkill.name returns skillName
    foundSkill.areChangesAllowed() returns false
    foundSkill.isAllowedValue(givenValue) returns true
    foundSkill.value returns None

    registryMock.get(givenUuid) returns List(foundSkill)

    val createdSkill = mock[Skill[String]]
    foundSkill.mutate(givenValue) returns createdSkill

    testUnit.calculateValue(givenUuid, givenSkill, givenValue) should be(None)

    skillOpsMock.get(skillName) wasNever called
    foundSkill.mutate(*) wasNever called
  }

  it should "returns None if the value is not valid" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenValue = Option("test-value")

    val foundSkill = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]
    foundSkill.name returns skillName
    foundSkill.areChangesAllowed() returns true
    foundSkill.isAllowedValue(givenValue) returns false
    foundSkill.value returns None

    registryMock.get(givenUuid) returns List(foundSkill)

    val createdSkill = mock[Skill[String]]
    foundSkill.mutate(givenValue) returns createdSkill

    testUnit.calculateValue(givenUuid, givenSkill, givenValue) should be(None)

    skillOpsMock.get(skillName) wasNever called
    foundSkill.mutate(*) wasNever called
  }

  it should "returns None if the value has not changed" in {
    val givenUuid = UUID.randomUUID()
    val skillName = ResourceLocation("skillstest", "test_skill").get

    val givenSkill = mock[Skill[String]]
    givenSkill.name returns skillName

    val givenValue = Option("test-value")

    val foundSkill = mock[Skill[String] with ChangeableSkillOps[String, Skill[String]]]
    foundSkill.name returns skillName
    foundSkill.areChangesAllowed() returns true
    foundSkill.isAllowedValue(givenValue) returns true
    foundSkill.value returns givenValue

    registryMock.get(givenUuid) returns List(foundSkill)

    val createdSkill = mock[Skill[String]]
    foundSkill.mutate(givenValue) returns createdSkill

    testUnit.calculateValue(givenUuid, givenSkill, givenValue) should be(None)

    skillOpsMock.get(skillName) wasNever called
    foundSkill.mutate(*) wasNever called
  }

  "Player.apply" should "return a Player instance" in {
    val facade = Player(registryMock, skillTypeOpsMock, skillOpsMock)

    facade.isInstanceOf[Player] should be(true)
  }

  "Player.apply" should "return a Player instance without parameters" in {
    val facade = Player()

    facade.isInstanceOf[Player] should be(true)
  }
}
