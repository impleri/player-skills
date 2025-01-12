package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import org.mockito.captor.ArgCaptor

class ItemRestrictionBuilderSpec extends BaseSpec {
  private val mockRegistry = mock[Registry.ITEM]
  private val mockRestrictions = mock[RestrictionRegistry]
  private val mockLogger = mock[Logger]

  private val testUnit = ItemRestrictionBuilder(Option(mockRegistry), mockRestrictions, mockLogger)

  private case class TestConditions() extends ItemConditions {
    override def name: ResourceLocation = ResourceLocation("skillstest", "condition").get
  }

  private val testBuilder = TestConditions()

  private val mockItem = mock[Item]

  "ItemRestrictionBuilder.restrictOne" should "restrict a simple item" in {
    val targetName = ResourceLocation("skillstest", "restriction").get

    mockRegistry.find(targetName) returns Option(mockItem)
    testUnit.restrictOne(targetName, testBuilder)

    mockRestrictions.add(any[ItemRestriction]) wasCalled once
  }

  "ItemRestrictionBuilder.restrictString" should "restrict a parseable item" in {
    val targetName = "minecraft:diamond_sword{Enchantments:[{id:\"minecraft:sharpness\",lvl:10}]}"
    val captor = ArgCaptor[ItemRestriction]

    testUnit.restrictString(targetName, testBuilder)

    mockRestrictions.add(captor) wasCalled once

    captor.value.target.isEnchanted shouldBe true
  }
}
