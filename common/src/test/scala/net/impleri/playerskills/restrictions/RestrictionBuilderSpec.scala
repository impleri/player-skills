package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.impleri.slab.block.Block
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation

class RestrictionBuilderSpec extends BaseSpec {
  private val mockRegistry = mock[Registry.BLOCK]
  private val mockLogger = mock[Logger]
  private val restrictionId = "skillstest:something"
  private val mockRestrictString = mock[(String, TestConditionBuilder) => Unit]
  private val mockRestrictOne = mock[(ResourceLocation, TestConditionBuilder) => Unit]
  private val testName: ResourceLocation = ResourceLocation("skillstest", "test").get
  private val mockRegistryKey = mock[ResourceKey.VanillaRegistry[Block.Vanilla]]

  private case class TestConditionBuilder(
      target: String = "target",
    override val name: ResourceLocation = testName,
  ) extends RestrictionConditionsBuilder {
    override def isValid: Boolean = true

    override def getTarget: String = target
  }

  private case class TestRestrictionBuilder(override val singleAsString: Boolean = false)
    extends RestrictionBuilder[Block, Block.Vanilla, TestConditionBuilder] {
    override protected val registry: Option[Registry.BLOCK] = Option(mockRegistry)
    protected val logger: Logger = mockLogger

    override protected def restrictString(
      targetName: String,
      builder: TestConditionBuilder,
    ): Unit = {
      mockRestrictString(targetName, builder)
    }

    override protected def restrictOne(
      targetName: ResourceLocation,
      builder: TestConditionBuilder,
    ): Unit = {
      mockRestrictOne(targetName, builder)
    }
  }

  private val testUnit = TestRestrictionBuilder()

  mockRegistry.name returns mockRegistryKey

  "RestrictionBuilder.add" should "produce restrictions for a namespace" in {
    val restrictionName = "skillstest"
    val namespace = s"@$restrictionName"
    val conditionBuilder = TestConditionBuilder(namespace)
    val targetName = ResourceLocation("skillstest", "item").get

    mockRegistry.matchingNamespace(restrictionName) returns List(targetName)
    testUnit.add(restrictionId, conditionBuilder)

    testUnit.restrictions(restrictionId) shouldBe conditionBuilder

    testUnit.commit()

    mockRegistry.matchingNamespace(restrictionName) wasCalled once
    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a tag" in {
    //    val registryName = ResourceLocation("skillstest", "registry").get
    //    mockRegistry.name returns ResourceKey
    //      .forRegistry(registryName)
    //      .value
    //      .asInstanceOf[ResourceKey.VanillaRegistry[Block.Vanilla]]

    val restrictionName = "skillstest:tag"
    val tag = s"#$restrictionName"
    val conditionBuilder = TestConditionBuilder(tag)
    val targetName = ResourceLocation("skillstest", "item").get

    mockRegistry.matchingTag(*) returns List(targetName)
    testUnit.add(restrictionId, conditionBuilder)

    testUnit.restrictions(restrictionId) shouldBe conditionBuilder
    testUnit.commit()

    mockRegistry.matchingTag(*) wasCalled once
    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a single resource" in {
    val restrictionName = "skillstest:item"
    val conditionBuilder = TestConditionBuilder(restrictionName)
    val targetName = ResourceLocation("skillstest", "item").get

    testUnit.add(restrictionId, conditionBuilder)

    testUnit.restrictions(restrictionId) shouldBe conditionBuilder
    testUnit.commit()

    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a single string" in {
    val testUnit = TestRestrictionBuilder(true)
    val restrictionName = "skillstest:item"
    val conditionBuilder = TestConditionBuilder(restrictionName)

    testUnit.add(restrictionId, conditionBuilder)

    testUnit.restrictions(restrictionId) shouldBe conditionBuilder
    testUnit.commit()

    mockRestrictString(restrictionName, conditionBuilder) wasCalled once
  }

  "RestrictionBuilder.logRestriction" should "log restriction metadata" in {
    val restrictionName = "skillstest"
    val restriction = mock[Restriction[Block, Block.Vanilla]]
    restriction.includeBiomes returns Seq.empty
    restriction.excludeBiomes returns Seq.empty
    restriction.includeDimensions returns Seq.empty
    restriction.excludeDimensions returns Seq.empty
    testUnit.logRestriction(restrictionName, restriction)

    mockLogger.infoP(*)(*) wasCalled atLeastOnce
  }
}
