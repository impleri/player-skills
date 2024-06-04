package net.impleri.playerskills.facades.minecraft.core

import net.impleri.playerskills.BaseSpec
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.resources.{ResourceLocation => McResourceLocation}

class ResourceLocationSpec extends BaseSpec {
  private val namespace = "skilltest"
  private val path = "example"
  private val defaultResource = new McResourceLocation("skilltest", "example")

  "SkillResourceLocation.apply" should "return a ResourceLocation from strings" in {
    ResourceLocation(namespace, path).value should be(defaultResource)
  }

  it should "parse a ResourceLocation from a string using the provided namespace" in {
    ResourceLocation(s"$namespace:$path").value should be(defaultResource)
  }
}
