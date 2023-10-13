package net.impleri.playerskills.server.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

import java.util.UUID

class PlayerRegistrySpec extends BaseSpec {
  private case class TestSkill(
    override val name: ResourceLocation,
    override val value: Option[String] = None,
  ) extends Skill[String]

  private val playerOne = UUID.randomUUID()
  private val playerTwo = UUID.randomUUID()

  private val testName = new ResourceLocation("skills", "test")
  private val testSkill = TestSkill(testName)
  private val otherName = new ResourceLocation("skills", "other")
  private val otherSkill = TestSkill(otherName)

  private val skillRegistryMock = mock[SkillRegistry]
  private val loggerMock = mock[PlayerSkillsLogger]
  private val storageMock = mock[PlayerStorageIO]

  "PlayerRegistry.apply" should "return the correct class" in {
    val target = PlayerRegistry(skillsRegistry = skillRegistryMock, logger = loggerMock)

    target.entries.isEmpty should be(true)
    target.storage should be(None)
  }

  "PlayerRegistry.open" should "return a new state after opening a players" in {
    skillRegistryMock.entries returns List(testSkill)
    storageMock.read(*) returns List.empty

    val target = PlayerRegistry(storageMock, PlayerRegistryState.empty, skillRegistryMock, loggerMock)

    target.entries.isEmpty should be(true)

    target.open(List(playerOne, playerTwo))

    target.entries.isEmpty should be(false)
  }

  it should "ensure skills in memory are still valid" in {
    skillRegistryMock.entries returns List(testSkill)
    storageMock.read(playerOne) returns List(otherSkill)

    val target = PlayerRegistry(storageMock, PlayerRegistryState.empty, skillRegistryMock, loggerMock)

    val response = target.open(playerOne)

    response should be(List(testSkill))
  }

  "PlayerRegistry.has" should "return true if the player is found in memory" in {
    val (state, _) = PlayerRegistryState
      .upsert(playerOne, List(testSkill, otherSkill))
      .run(PlayerRegistryState.empty)
      .value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.has(playerOne) should be(true)
  }

  it should "return false if the player is not found in memory" in {
    val target = PlayerRegistry(storageMock, PlayerRegistryState.empty, skillRegistryMock, loggerMock)

    target.has(playerOne) should be(false)
  }

  "PlayerRegistry.get" should "return all the skills of the player from memory" in {
    val (state, _) = PlayerRegistryState
      .upsert(playerOne, List(testSkill, otherSkill))
      .run(PlayerRegistryState.empty)
      .value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.get(playerOne) should be(List(testSkill, otherSkill))
  }

  it should "return an empty list if the player is not in memory" in {
    val (state, _) = PlayerRegistryState
      .upsert(playerOne, List(testSkill, otherSkill))
      .run(PlayerRegistryState.empty)
      .value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.get(playerTwo) should be(List.empty)
  }

  "PlayerRegistry.upsert" should "replace a skill in state" in {
    val (state, _) = PlayerRegistryState.upsert(playerOne, List(otherSkill)).run(PlayerRegistryState.empty).value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    val newSkill = TestSkill(testName, Some("test-value"))

    target.upsert(playerOne, newSkill)

    target.get(playerOne).find(_.name == testName).value should be(newSkill)
  }

  "PlayerRegistry.addSkill" should "adds a skill to the player in memory if it is not already there" in {
    val (state, _) = PlayerRegistryState.upsert(playerOne, List(otherSkill)).run(PlayerRegistryState.empty).value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.addSkill(playerOne, testSkill)

    target.get(playerOne).find(_.name == testName).value should be(testSkill)
  }

  it should "not add a skill to the player in memory if it is already there" in {
    val (state, _) = PlayerRegistryState.upsert(playerOne, List(testSkill)).run(PlayerRegistryState.empty).value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.addSkill(playerOne, TestSkill(testName, Some("test-value")))

    target.get(playerOne).find(_.name == testName).value should be(testSkill)
  }

  "PlayerRegistry.removeSkill" should "removes a skill from the player in memory if it is already there" in {
    val (state, _) = PlayerRegistryState
      .upsert(playerOne, List(TestSkill(testName, Some("test-value")), otherSkill))
      .run(PlayerRegistryState.empty)
      .value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.removeSkill(playerOne, testSkill)

    target.get(playerOne).find(_.name == testName) should be(None)
  }

  "PlayerRegistry.close" should "remove a player info from memory and sync to storage" in {
    val (state, _) = PlayerRegistryState
      .upsert(playerOne, List(testSkill, otherSkill))
      .run(PlayerRegistryState.empty)
      .value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.has(playerOne) should be(true)

    target.close(playerOne) should be(true)

    target.has(playerOne) should be(false)
  }

  it should "empty the state" in {
    val (initialState, _) = PlayerRegistryState
      .upsert(playerOne, List(testSkill, otherSkill))
      .run(PlayerRegistryState.empty)
      .value
    val (state, _) = PlayerRegistryState.upsert(playerTwo, List.empty).run(initialState).value

    val target = PlayerRegistry(storageMock, state, skillRegistryMock, loggerMock)

    target.close()

    target.entries.isEmpty should be(true)
  }
}
