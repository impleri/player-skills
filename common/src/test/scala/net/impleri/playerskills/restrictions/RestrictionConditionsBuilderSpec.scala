package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.conditions.{RestrictionConditionsBuilder, SingleTargetRestriction}
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation

class RestrictionConditionsBuilderSpec extends BaseSpec {
  private val testName: ResourceLocation = ResourceLocation("skillstest", "test").get

  private case class TestConditionBuilder(
    override val name: ResourceLocation = testName,
  ) extends RestrictionConditionsBuilder with SingleTargetRestriction[String] {
    override def isValid: Boolean = true

    override def getTarget: String = "target"
  }

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
    val testValue = mock[Player => Boolean]

    testUnit.predicate(testValue)
    testUnit.condition shouldBe testValue
  }

  "PlayerConditions.unless" should "change the condition to a negative" in {
    val testValue = mock[Player => Boolean]
    testValue(*) returns true

    val mockPlayer = mock[Player]

    testUnit.unless(testValue)
    testUnit.condition(mockPlayer) shouldBe false
  }
}
