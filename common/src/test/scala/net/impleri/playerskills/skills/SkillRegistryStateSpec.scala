package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

class SkillRegistryStateSpec extends BaseSpec {
  private case class TestSkill(
    override val name: ResourceLocation,
    override val value: Option[String] = None,
  ) extends Skill[String]

  "SkillRegistryState.empty" should "create an empty state" in {
    val state = SkillRegistryState.empty

    SkillRegistryState.entries().run(state).value._2.isEmpty should be(true)
  }

  "SkillRegistryState.add" should "return a new state with the added skill" in {
    val initialState = SkillRegistryState.empty
    val expectedName = ResourceLocation("skills", "test").get
    val unneededName = ResourceLocation("skills", "bad").get
    val testSkill = TestSkill(expectedName)

    val (nextState, _) = SkillRegistryState.add(testSkill).run(initialState).value

    SkillRegistryState.entries().run(nextState).value._2.size should be(1)
    SkillRegistryState.has(expectedName).run(nextState).value._2 should be(true)
    SkillRegistryState.has(unneededName).run(nextState).value._2 should be(false)
  }

  it should "throw an error if the skill is already added" in {
    val initialState = SkillRegistryState.empty
    val skillName = ResourceLocation("skills", "testname").get
    val testSkill = TestSkill(skillName)

    val (nextState, _) = SkillRegistryState.add(testSkill).run(initialState).value
    val result = SkillRegistryState.add(testSkill).run(nextState)

    result.isLeft should be(true)
    result.left.value.getMessage.contains("testname") should be(true)
  }

  "SkillRegistryState.upsert" should "return a new state with the added skill" in {
    val initialState = SkillRegistryState.empty
    val expectedName = ResourceLocation("skills", "test").get
    val unneededName = ResourceLocation("skills", "bad").get
    val testSkill = TestSkill(expectedName)

    val (nextState, _) = SkillRegistryState.upsert(testSkill).run(initialState).value

    SkillRegistryState.entries().run(nextState).value._2.size should be(1)
    SkillRegistryState.find(expectedName).run(nextState).value._2.isEmpty should be(false)
    SkillRegistryState.find(unneededName).run(nextState).value._2.isEmpty should be(true)
  }

  it should "replace the existing skill rather than add a new one" in {
    val initialState = SkillRegistryState.empty
    val expectedName = ResourceLocation("skills", "test").get
    val unneededName = ResourceLocation("skills", "bad").get
    val firstSkill = TestSkill(expectedName)
    val replacedSkill = TestSkill(expectedName, Option("test"))

    val (nextState, _) = SkillRegistryState.upsert(firstSkill).run(initialState).value
    val (finalState, _) = SkillRegistryState.upsert(replacedSkill).run(nextState).value

    SkillRegistryState.entries().run(finalState).value._2.size should be(1)
    SkillRegistryState.find(expectedName).run(finalState).value._2.isEmpty should be(false)
    SkillRegistryState.find(unneededName).run(finalState).value._2.isEmpty should be(true)
  }

  "SkillRegistryState.resync" should "return a new state with the synced skills" in {
    val initialState = SkillRegistryState.empty
    val expectedName = ResourceLocation("skills", "test").get
    val unneededName = ResourceLocation("skills", "bad").get
    val testSkill = TestSkill(expectedName)
    val badSkill = TestSkill(unneededName)

    val (nextState, _) = SkillRegistryState.upsert(badSkill).run(initialState).value
    val (finalState, _) = SkillRegistryState.resync(List(testSkill)).run(nextState).value

    SkillRegistryState.entries().run(finalState).value._2.size should be(1)
    SkillRegistryState.has(expectedName).run(finalState).value._2 should be(true)
    SkillRegistryState.has(unneededName).run(finalState).value._2 should be(false)
  }

  "SkillRegistryState.remove" should "return a new state with the skill removed" in {
    val initialState = SkillRegistryState.empty
    val expectedName = ResourceLocation("skills", "test").get
    val unneededName = ResourceLocation("skills", "other").get
    val testSkill = TestSkill(expectedName)
    val otherSkill = TestSkill(unneededName)

    val (nextState, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(initialState).value
    val (finalState, _) = SkillRegistryState.remove(otherSkill.name).run(nextState).value

    SkillRegistryState.entries().run(finalState).value._2.size should be(1)
    SkillRegistryState.has(expectedName).run(finalState).value._2 should be(true)
    SkillRegistryState.has(unneededName).run(finalState).value._2 should be(false)
  }
}
