package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.{Recipe => McRecipe}

class TargetResourceSpec extends BaseSpec {
  "TargetResource.apply" should "return a Namespace target if starts with @" in {
    TargetResource("@test_namespace").value shouldBe TargetResource.Namespace("test_namespace")
  }

  it should "return a Namespace target if ends with :*" in {
    TargetResource("test_namespace:*").value shouldBe TargetResource.Namespace("test_namespace")
  }

  it should "return None if starts with # and there is no registry key provided" in {
    TargetResource("#skillstest:tag") shouldBe None
  }

  it should "return Tag target if starts with # and there is a provided registry key" in {
    val registryKeyName = ResourceLocation("skillstest:registry", isSkill = false).get
    val mockRegistryKey = ResourceKey.createRegistryKey[McRecipe[_]](registryKeyName.name)
    val tagName = ResourceLocation("skillstest:tag", isSkill = false).get

    TargetResource("#skillstest:tag", Option(mockRegistryKey)).value shouldBe TargetResource
      .Tag(tagName.getTagKey(mockRegistryKey))
  }

  it should "return a Single target if it is a valid ResourceLocation" in {
    TargetResource("skillstest:target").value shouldBe TargetResource
      .Single(ResourceLocation("skillstest:target").get)
  }

  it should "return None if it is not a valid ResourceLocation" in {
    TargetResource("skillsTest:target") shouldBe None
  }

  it should "return a SingleString target if it is not a namespace or tag" in {
    TargetResource("skillsTest:target", singleAsString = true).value shouldBe TargetResource
      .SingleString("skillsTest:target")
  }
}
