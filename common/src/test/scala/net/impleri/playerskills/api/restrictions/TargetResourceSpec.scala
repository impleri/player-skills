package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation

class TargetResourceSpec extends BaseSpec {
  "TargetResource.apply" should "return a Namespace target if starts with @" in {
    TargetResource.create("@test_namespace").value shouldBe TargetResource.Namespace("test_namespace")
  }

  it should "return a Namespace target if ends with :*" in {
    TargetResource.create("test_namespace:*").value shouldBe TargetResource.Namespace("test_namespace")
  }

  it should "return None if starts with # and there is no registry key provided" in {
    TargetResource.create("#skillstest:tag") shouldBe None
  }

  it should "return Tag target if starts with # and there is a provided registry key" in {
    val registryKeyName = ResourceLocation("skillstest:registry").get
    val mockRegistryKey = ResourceKey.forVanillaRegistry[Item, Item.Vanilla](registryKeyName)
    val tagName = ResourceLocation("skillstest:tag").get

    TargetResource.create("#skillstest:tag", Option(mockRegistryKey)).value shouldBe TargetResource
      .Tag(tagName.getTagKey[Item, Item.Vanilla](mockRegistryKey))
  }

  it should "return a Single target if it is a valid ResourceLocation" in {
    TargetResource.create("skillstest:target").value shouldBe TargetResource
      .Single(ResourceLocation("skillstest:target").get)
  }

  it should "return None if it is not a valid ResourceLocation" in {
    TargetResource.create("skillsTest:target") shouldBe None
  }

  it should "return a SingleString target if it is not a namespace or tag" in {
    TargetResource.create("skillsTest:target", singleAsString = true).value shouldBe TargetResource
      .SingleString("skillsTest:target")
  }
}
