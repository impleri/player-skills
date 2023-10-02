package net.impleri.playerskills.utils

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.ResourceLocation

class SkillResourceLocationSpec extends BaseSpec {
  private val namespace = "skilltest"
  private val path = "example"
  private val defaultResource = new ResourceLocation("skilltest", "example")

  it should "return a ResourceLocation from strings" in {
    SkillResourceLocation.of(namespace, path) should be (Some(defaultResource))
  }

  it should "parse a ResourceLocation from a string using the skills namespace" in {
    val expected = new ResourceLocation(SkillResourceLocation.DEFAULT_NAMESPACE, path)
    SkillResourceLocation.of(path) should be (Some(expected))
  }

  it should "parse a ResourceLocation from a string using the provided namespace" in {
    SkillResourceLocation.of(s"$namespace:$path") should be (Some(defaultResource))
  }

  "SkillResourceLocation.ofMinecraft" should "return the output from of if parsing fails" in {
    SkillResourceLocation.ofMinecraft("test:camelCase") should be (None)
  }

  "SkillResourceLocation.ofMinecraft" should "return the vanilla parsed ResourceLocation if parsing succeeds" in {
    SkillResourceLocation.ofMinecraft(s"$namespace:$path") should be (Some(defaultResource))
  }

  "SkillResourceLocation.ofMinecraft" should "return the vanilla namespace ResourceLocation if parsing succeeds" in {
    val expected = new ResourceLocation("minecraft", path)
    SkillResourceLocation.ofMinecraft(path) should be (Some(expected))
  }
}
