package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.architectury.Registrar
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

class SkillRegistrySpec extends BaseSpec {
  private case class TestSkill(
    override val name: ResourceLocation,
    override val value: Option[String] = None,
  ) extends Skill[String]

  private val testName = new ResourceLocation("skills", "test")
  private val testSkill = TestSkill(testName)
  private val otherName = new ResourceLocation("skills", "other")
  private val otherSkill = TestSkill(otherName)

  private val emptyState = SkillRegistryState.empty
  private val registrarMock = mock[Registrar[Skill[_]]]

  "SkillRegistry.apply" should "return the correct class" in {
    val target = SkillRegistry(emptyState, registrarMock)

    target.entries.isEmpty should be(true)
  }

  "SkillRegistryState.resync" should "return a new state with the synced skills" in {
    val registryKey = mock[ResourceKey[Registry[Skill[_]]]]
    val entries = Map[ResourceKey[Skill[_]], Skill[_]](
      ResourceKey.create(registryKey, testName) -> testSkill,
      ResourceKey.create(registryKey, otherName) -> otherSkill,
    )
    registrarMock.entries() returns entries

    val target = SkillRegistry(emptyState, registrarMock)

    target.entries.isEmpty should be(true)

    target.resync()

    target.entries.isEmpty should be(false)
  }

  "SkillRegistry.has" should "return true if there is a skill with the given name" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.has(testName) should be(true)
  }

  it should "return true if there is a skill with a matching name" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.has(testSkill) should be(true)
  }

  it should "return false if there is no skill with the given name" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.has(new ResourceLocation("skills", "bad")) should be(false)
  }

  "SkillRegistry.find" should "return a skill with the given name" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.find(testName).value should be(testSkill)
  }

  it should "return nothing if there is no match" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.find(new ResourceLocation("skills", "bad")) should be(None)
  }

  "SkillRegistry.upsert" should "replace a skill in state" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    val newSkill = TestSkill(testName, Option("test-value"))

    target.upsert(newSkill)

    target.find(testName).value should be(newSkill)
  }

  "SkillRegistry.add" should "insert a new skill into state" in {
    val target = SkillRegistry(gameRegistrar = registrarMock)

    target.find(testName) should be(None)

    target.add(testSkill)

    target.find(testName).value should be(testSkill)
  }

  "SkillRegistry.removeSkill" should "remove a skill from state" in {
    val (state, _) = SkillRegistryState.resync(List(testSkill, otherSkill)).run(emptyState).value

    val target = SkillRegistry(state, registrarMock)

    target.find(testName).value should be(testSkill)

    target.removeSkill(testSkill)

    target.find(testName) should be(None)
  }
}
