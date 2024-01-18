package net.impleri.playerskills.facades.minecraft.core

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.{ResourceLocation => McResourceLocation}

class ResourceLocationSpec extends BaseSpec {
  private val namespace = "skilltest"
  private val path = "example"
  private val defaultResource = new McResourceLocation("skilltest", "example")

  "SkillResourceLocation.apply" should "return a ResourceLocation from strings" in {
    ResourceLocation(namespace, path).value should be(defaultResource)
  }

  it should "parse a ResourceLocation from a string using the skills namespace" in {
    val expected = new McResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, path)
    ResourceLocation(path).value should be(expected)
  }

  it should "parse a ResourceLocation from a string using the provided namespace" in {
    ResourceLocation(s"$namespace:$path").value should be(defaultResource)
  }

  it should "return None if parsing fails" in {
    ResourceLocation("test:camelCase", isSkill = false) should be(None)
  }

  it should "return the vanilla parsed ResourceLocation if parsing succeeds" in {
    ResourceLocation(s"$namespace:$path", isSkill = false).value should be(defaultResource)
  }

  it should "return the vanilla namespace ResourceLocation if parsing succeeds" in {
    val expected = new McResourceLocation("minecraft", path)
    ResourceLocation(path, isSkill = false).value should be(expected)
  }
}
