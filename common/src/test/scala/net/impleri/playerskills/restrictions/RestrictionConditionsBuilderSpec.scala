package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation

class RestrictionConditionsBuilderSpec extends BaseSpec {
  private val testName: ResourceLocation = new ResourceLocation("skillstest", "test")

  private case class TestConditionBuilder(
    override val name: ResourceLocation = testName,
  ) extends RestrictionConditionsBuilder {}

  private val testUnit = TestConditionBuilder()

  "BiomeConditions.inBiome" should "add to includeBiomes" in {
    val testValue = "skillsinclude:biome"

    testUnit.inBiome(testValue)
    testUnit.includeBiomes.contains(testValue) shouldBe true
    testUnit.excludeBiomes.isEmpty shouldBe true
  }

  "BiomeConditions.notInBiome" should "add to excludeBiomes" in {
    val testValue = "skillsexclude:biome"

    testUnit.notInBiome(testValue)
    testUnit.excludeBiomes.contains(testValue) shouldBe true
    testUnit.includeBiomes.isEmpty shouldBe true
  }

  "DimensionConditions.inDimension" should "add to includeDimensions" in {
    val testValue = "skillsinclude:dimension"

    testUnit.inDimension(testValue)
    testUnit.includeDimensions.contains(testValue) shouldBe true
    testUnit.excludeDimensions.isEmpty shouldBe true
  }

  "DimensionConditions.notInDimension" should "add to excludeDimensions" in {
    val testValue = "skillsexclude:dimension"

    testUnit.notInDimension(testValue)
    testUnit.excludeDimensions.contains(testValue) shouldBe true
    testUnit.includeDimensions.isEmpty shouldBe true
  }

  "PlayerConditions.predicate" should "change the condition" in {
    val testValue = mock[Player[_] => Boolean]

    testUnit.predicate(testValue)
    testUnit.condition shouldBe testValue
  }

  "PlayerConditions.unless" should "change the condition to a negative" in {
    val testValue = mock[Player[_] => Boolean]
    testValue(*) returns true

    val mockPlayer = mock[Player[_]]

    testUnit.unless(testValue)
    testUnit.condition(mockPlayer) shouldBe false
  }
}
