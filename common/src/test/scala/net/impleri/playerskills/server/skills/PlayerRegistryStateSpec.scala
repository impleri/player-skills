package net.impleri.playerskills.server.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.minecraft.resources.ResourceLocation

import java.util.UUID

class PlayerRegistryStateSpec extends BaseSpec {
  private case class TestSkill(
    override val name: ResourceLocation,
    override val value: Option[String] = None
  ) extends Skill[String]

  "PlayerRegistryState.empty" should "create an empty state" in {
    val state = PlayerRegistryState.empty

    PlayerRegistryState.entries().run(state).value._2.isEmpty should be(true)
  }

  "PlayerRegistryState.upsert" should "return a new state with the added player" in {
    val givenPlayer = UUID.randomUUID()

    val initialState = PlayerRegistryState.empty
    val testName = new ResourceLocation("skills", "test")
    val testSkill = TestSkill(testName)

    val (nextState, _) = PlayerRegistryState.upsert(givenPlayer, List(testSkill)).run(initialState).value

    PlayerRegistryState.entries().run(nextState).value._2.size should be(1)
    PlayerRegistryState.has(givenPlayer).run(nextState).value._2 should be(true)
    PlayerRegistryState.has(UUID.randomUUID()).run(nextState).value._2 should be(false)
  }

  it should "replace the existing player rather than add a new one" in {
    val givenPlayer = UUID.randomUUID()

    val initialState = PlayerRegistryState.empty
    val expectedName = new ResourceLocation("skills", "test")
    val unneededName = new ResourceLocation("skills", "bad")
    val firstSkill = TestSkill(expectedName)
    val replacedSkill = TestSkill(expectedName, Some("test"))

    val (nextState, _) = PlayerRegistryState.upsert(givenPlayer, List(firstSkill, TestSkill(unneededName))).run(initialState).value
    val (finalState, _) = PlayerRegistryState.upsert(givenPlayer, List(replacedSkill)).run(nextState).value

    PlayerRegistryState.entries().run(finalState).value._2.size should be(1)

    val receivedSkills = PlayerRegistryState.get(givenPlayer).run(finalState).value._2
    receivedSkills.isEmpty should be(false)
    receivedSkills.contains(replacedSkill) should be(true)
    receivedSkills.contains(firstSkill) should be(false)
  }

  "PlayerRegistryState.upsertMany" should "return a new state with the added players" in {
    val givenPlayer = UUID.randomUUID()
    val otherPlayer = UUID.randomUUID()

    val initialState = PlayerRegistryState.empty
    val testName = new ResourceLocation("skills", "test")
    val otherName = new ResourceLocation("skills", "other")
    val testSkill = TestSkill(testName)

    val givenMap = Map(
      givenPlayer -> List(testSkill),
      otherPlayer -> List(TestSkill(otherName))
    )

    val (nextState, _) = PlayerRegistryState.upsertMany(givenMap).run(initialState).value

    PlayerRegistryState.entries().run(nextState).value._2.size should be(2)
    PlayerRegistryState.has(givenPlayer).run(nextState).value._2 should be(true)
    PlayerRegistryState.has(otherPlayer).run(nextState).value._2 should be(true)
    PlayerRegistryState.has(UUID.randomUUID()).run(nextState).value._2 should be(false)
  }

  "PlayerRegistryState.remove" should "return a new state with the skill removed" in {
    val givenPlayer = UUID.randomUUID()

    val initialState = PlayerRegistryState.empty
    val expectedName = new ResourceLocation("skills", "test")
    val unneededName = new ResourceLocation("skills", "other")
    val testSkill = TestSkill(expectedName)

    val (nextState, _) = PlayerRegistryState.upsert(givenPlayer, List(testSkill, TestSkill(unneededName))).run(initialState).value
    val (finalState, _) = PlayerRegistryState.remove(givenPlayer).run(nextState).value

    PlayerRegistryState.entries().run(finalState).value._2.isEmpty should be(true)
    PlayerRegistryState.has(givenPlayer).run(finalState).value._2 should be(false)
  }

  "PlayerRegistryState.removeMany" should "return a new state with the removed players" in {
    val givenPlayer = UUID.randomUUID()
    val otherPlayer = UUID.randomUUID()

    val initialState = PlayerRegistryState.empty
    val testName = new ResourceLocation("skills", "test")
    val otherName = new ResourceLocation("skills", "other")
    val testSkill = TestSkill(testName)

    val givenMap = Map(
      givenPlayer -> List(testSkill),
      otherPlayer -> List(TestSkill(otherName))
    )

    val (nextState, _) = PlayerRegistryState.upsertMany(givenMap).run(initialState).value
    val (finalState, _) = PlayerRegistryState.removeMany(List(givenPlayer, otherPlayer)).run(nextState).value

    PlayerRegistryState.entries().run(finalState).value._2.isEmpty should be(true)
    PlayerRegistryState.has(givenPlayer).run(finalState).value._2 should be(false)
    PlayerRegistryState.has(otherPlayer).run(finalState).value._2 should be(false)
    PlayerRegistryState.has(UUID.randomUUID()).run(finalState).value._2 should be(false)
  }
}
