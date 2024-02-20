package net.impleri.playerskills.restrictions.item

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.core.{ResourceLocation => ResourceFacade}
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import org.mockito.captor.ArgCaptor

class ItemRestrictionBuilderSpec extends BaseSpec {
  private val mockRegistry = mock[Registry[Item]]
  private val mockRestrictions = mock[RestrictionRegistry]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = ItemRestrictionBuilder(Option(mockRegistry), mockRestrictions, mockLogger)

  private case class TestConditions() extends ItemConditions {
    override def name: ResourceLocation = new ResourceLocation("skillstest", "condition")
  }

  private val testBuilder = TestConditions()

  private val mockItem = mock[Item]

  "ItemRestrictionBuilder.restrictOne" should "restrict a simple item" in {
    val targetName = ResourceFacade("skillstest", "restriction").get

    mockRegistry.get(targetName) returns Option(mockItem)
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
