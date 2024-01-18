package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Biome

class RestrictionSpec extends BaseSpec {
  private val mockItem = mock[Item]

  private case class TestRestriction(
    override val target: Item = mockItem,
    override val condition: Player[_] => Boolean = _ => true,
    override val includeDimensions: Seq[String] = Seq.empty,
    override val excludeDimensions: Seq[String] = Seq.empty,
    override val includeBiomes: Seq[String] = Seq.empty,
    override val excludeBiomes: Seq[String] = Seq.empty,
    override val replacement: Option[Item] = None,
  ) extends Restriction[Item] {
    override def restrictionType: RestrictionType = RestrictionType.Item()
  }

  "Restriction.isType" should "return true if the type matches" in {
    TestRestriction().isType(RestrictionType.Item()) should be(true)
  }

  it should "return false if the type does not match" in {
    TestRestriction().isType(RestrictionType.Recipe()) should be(false)
  }

  "Restriction.targets" should "return true if the target name matches" in {
    val givenSkill = ResourceLocation("skillstest:skill")
    mockItem.getName returns givenSkill
    TestRestriction().targets(givenSkill.get) should be(true)
  }

  it should "return false if the target name does not matches" in {
    val givenSkill = ResourceLocation("skillstest:skill")
    mockItem.getName returns givenSkill

    TestRestriction().targets(ResourceLocation("skillstest:other").get) should be(false)
  }

  "Restriction.hasReplacement" should "return false if the replacement is None" in {
    TestRestriction().hasReplacement should be(false)
  }


  "Restriction.isAllowedDimension" should "return true if the given parameter is in the include list and not in the exclude list" in {
    val testDimension = "skillstest:nether"
    val givenDimension = ResourceLocation(testDimension)
    val includeDimensions = Seq("skillstest:overworld", testDimension, "skillsTest:badValue")
    TestRestriction(includeDimensions = includeDimensions).isAllowedDimension(givenDimension.get) should be(true)
  }

  it should "return false if the given parameter is in the include list and in the exclude list" in {
    val testDimension = "skillstest:nether"
    val givenDimension = ResourceLocation(testDimension)
    val includeDimensions = Seq("skillstest:*")
    val excludeDimensions = Seq(testDimension)
    TestRestriction(includeDimensions = includeDimensions, excludeDimensions = excludeDimensions)
      .isAllowedDimension(givenDimension.get) should be(false)
  }

  it should "return false if the given parameter is not in the include list" in {
    val testDimension = "skillstest:nether"
    val givenDimension = ResourceLocation(testDimension)
    TestRestriction().isAllowedDimension(givenDimension.get) should be(false)
  }

  "Restriction.isAllowedBiome" should "return true if the given parameter is in the include list and not in the exclude list" in {
    val testBiome = "skillstest:plains"
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)
    givenBiome.isNamed(ResourceLocation(testBiome).get) returns true

    val includeBiomes = Seq("skillstest:desert", testBiome, "skillsTest:badValue")
    TestRestriction(includeBiomes = includeBiomes).isAllowedBiome(givenBiome) should be(true)
  }

  it should "return false if the given parameter is in the include list and in the exclude list" in {
    val testBiome = "skillstest:nether"
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)

    givenBiome.isNamespaced("skillstest") returns true
    givenBiome.isNamed(ResourceLocation(testBiome).get) returns true

    val includeBiomes = Seq("@skillstest")
    val excludeBiomes = Seq(testBiome)
    TestRestriction(includeBiomes = includeBiomes, excludeBiomes = excludeBiomes)
      .isAllowedBiome(givenBiome) should be(false)
  }

  it should "return false if the given parameter is not in the include list" in {
    val testBiome = "skillstest:nether"
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)

    givenBiome.isNamed(*) returns false

    TestRestriction().isAllowedBiome(givenBiome) should be(false)
  }
}
