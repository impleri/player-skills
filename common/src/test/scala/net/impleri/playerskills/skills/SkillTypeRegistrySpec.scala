package net.impleri.playerskills.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.facades.architectury.Registrar
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.resources.ResourceKey

class SkillTypeRegistrySpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]

  private case class TestSkillType(
    override val name: ResourceLocation,
  ) extends SkillType[String] {
    override val skillOps: SkillOps = skillOpsMock

    override protected def castToString(value: String): Option[String] = Option("test-skill")

    override def castFromString(value: String): Option[String] = Option("test-value")

    override def getPrevValue(
      skill: Skill[String],
      min: Option[String],
      max: Option[String],
    ): Option[String] = {
      None
    }

    override def getNextValue(
      skill: Skill[String],
      min: Option[String],
      max: Option[String],
    ): Option[String] = {
      None
    }
  }

  "SkillTypeRegistry.resync" should "update state with the skills from the async registrar" in {
    val registrarMock = mock[Registrar[SkillType[_]]]

    val expectedName = ResourceLocation("skills", "test").get
    val expectedType = TestSkillType(expectedName)
    val resourceKey = mock[ResourceKey[SkillType[_]]]

    val registeredTypes = Map[ResourceKey[SkillType[_]], SkillType[_]](resourceKey -> expectedType)
    registrarMock.entries() returns registeredTypes

    val target = SkillTypeRegistry(registrarMock)
    target.resync()

    target.entries.size should be(1)
    target.find(expectedName).value should be(expectedType)
  }
}
