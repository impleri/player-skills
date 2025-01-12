package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.world.Biome

class RestrictionSpec extends BaseSpec {
  private val mockItem = mock[Item]

  private case class TestRestriction(
    override val target: Item = mockItem,
    override val condition: Player => Boolean = _ => true,
    override val includeDimensions: Seq[String] = Seq.empty,
    override val excludeDimensions: Seq[String] = Seq.empty,
    override val includeBiomes: Seq[String] = Seq.empty,
    override val excludeBiomes: Seq[String] = Seq.empty,
    override val replacement: Option[Item] = None,
  ) extends Restriction[Item, Item.Vanilla] {
    override def restrictionType: RestrictionType = RestrictionType.Item
  }

  private val testDimension = "skillstest:nether"
  private val otherDimension = "skillstest:tne_end"

  private val testBiome = "skillstest:plains"
  private val otherBiome = "skillstest:ocean"

  "Restriction.isType" should "return true if the type matches" in {
    TestRestriction().isType(RestrictionType.Item) should be(true)
  }

  it should "return false if the type does not match" in {
    TestRestriction().isType(RestrictionType.Recipe) should be(false)
  }

  "Restriction.targets" should "return true if the target name matches" in {
    val givenSkill = ResourceLocation("skillstest:skill")
    mockItem.name returns givenSkill
    TestRestriction().targets(givenSkill.get) should be(true)
  }

  it should "return false if the target name does not matches" in {
    val givenSkill = ResourceLocation("skillstest:skill")
    mockItem.name returns givenSkill

    TestRestriction().targets(ResourceLocation("skillstest:other").get) should be(false)
  }

  "Restriction.hasReplacement" should "return false if the replacement is None" in {
    TestRestriction().hasReplacement should be(false)
  }


  "Restriction.isAllowedDimension" should "return true if the given parameter is in the include list and not in the exclude list" in {
    val givenDimension = ResourceLocation(testDimension)
    val includeDimensions = Seq(testDimension)
    val excludeDimensions = Seq(otherDimension)

    TestRestriction(includeDimensions = includeDimensions, excludeDimensions = excludeDimensions)
      .isApplicableDimension(givenDimension.get) should be(true)
  }

  it should "return true if the given parameter is in the include list and the exclude list is empty" in {
    val givenDimension = ResourceLocation(testDimension)
    val includeDimensions = Seq(testDimension)

    TestRestriction(includeDimensions = includeDimensions)
      .isApplicableDimension(givenDimension.get) should be(true)
  }

  it should "return true if the given parameter is not in the exclude list and the include list is empty" in {
    val givenDimension = ResourceLocation(testDimension)
    val excludeDimensions = Seq(otherDimension)

    TestRestriction(excludeDimensions = excludeDimensions)
      .isApplicableDimension(givenDimension.get) should be(true)
  }

  it should "return true if the both the include list and the exclude list are empty" in {
    val givenDimension = ResourceLocation(testDimension)

    TestRestriction()
      .isApplicableDimension(givenDimension.get) should be(true)
  }

  it should "return false if the given parameter is in the include list and in the exclude list" in {
    val givenDimension = ResourceLocation(testDimension)
    val includeDimensions = Seq(testDimension)
    val excludeDimensions = Seq(testDimension)
    TestRestriction(includeDimensions = includeDimensions, excludeDimensions = excludeDimensions)
      .isApplicableDimension(givenDimension.get) should be(false)
  }

  it should "return false if the given parameter is not in the include list" in {
    val includeDimensions = Seq(otherDimension)
    val excludeDimensions = Seq("skillstest:overworld")
    val givenDimension = ResourceLocation(testDimension)
    TestRestriction(includeDimensions = includeDimensions, excludeDimensions = excludeDimensions)
      .isApplicableDimension(givenDimension.get) should be(false)
  }

  "Restriction.isAllowedBiome" should "return true if the given parameter is in the include list and not in the exclude list" in {
    val givenBiome = mock[Biome]
    val includeBiomes = Seq(testBiome)
    val excludeBiomes = Seq(otherBiome)

    givenBiome.name returns ResourceLocation(testBiome)
    givenBiome.isNamed(ResourceLocation(testBiome).get) returns true

    TestRestriction(includeBiomes = includeBiomes, excludeBiomes = excludeBiomes)
      .isApplicableBiome(givenBiome) should be(true)
  }

  it should "return true if the given parameter is in the include list and the exclude list is empty" in {
    val givenBiome = mock[Biome]
    val includeBiomes = Seq(testBiome)

    givenBiome.name returns ResourceLocation(testBiome)
    givenBiome.isNamed(ResourceLocation(testBiome).get) returns true

    TestRestriction(includeBiomes = includeBiomes)
      .isApplicableBiome(givenBiome) should be(true)
  }

  it should "return true if the given parameter is not in the exclude list and the include list is empty" in {
    val givenBiome = mock[Biome]
    val excludeBiomes = Seq(otherBiome)

    givenBiome.name returns ResourceLocation(testBiome)
    givenBiome.isNamed(ResourceLocation(otherBiome).get) returns false

    TestRestriction(excludeBiomes = excludeBiomes)
      .isApplicableBiome(givenBiome) should be(true)
  }

  it should "return true if the both the include list and the exclude list are empty" in {
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)

    TestRestriction()
      .isApplicableBiome(givenBiome) should be(true)
  }

  it should "return false if the given parameter is in the include list and in the exclude list" in {
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)

    givenBiome.isNamed(ResourceLocation(testBiome).get) returns true

    val includeBiomes = Seq(testBiome)
    val excludeBiomes = Seq(testBiome)
    TestRestriction(includeBiomes = includeBiomes, excludeBiomes = excludeBiomes)
      .isApplicableBiome(givenBiome) should be(false)
  }

  it should "return false if the given parameter is not in the include list" in {
    val givenBiome = mock[Biome]
    givenBiome.name returns ResourceLocation(testBiome)

    givenBiome.isNamed(*) returns false

    val includeBiomes = Seq(otherBiome)

    TestRestriction(includeBiomes = includeBiomes).isApplicableBiome(givenBiome) should be(false)
  }
}
