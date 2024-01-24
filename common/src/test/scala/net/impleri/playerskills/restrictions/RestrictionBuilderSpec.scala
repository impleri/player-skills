package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.facades.minecraft.core.{ResourceLocation => ResourceFacade}
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.world.{Block => BlockFacade}
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

class RestrictionBuilderSpec extends BaseSpec {
  private val mockRegistry = mock[Registry[Block]]
  private val mockLogger = mock[PlayerSkillsLogger]
  private val mockRestrictString = mock[(String, TestConditionBuilder) => Unit]
  private val mockRestrictOne = mock[(ResourceFacade, TestConditionBuilder) => Unit]
  private val testName: ResourceLocation = new ResourceLocation("skillstest", "test")

  private case class TestConditionBuilder(
    override val name: ResourceLocation = testName,
  ) extends RestrictionConditionsBuilder {}

  private case class TestRestrictionBuilder(override val singleAsString: Boolean = false)
    extends RestrictionBuilder[Block, TestConditionBuilder] {
    protected val registry: Registry[Block] = mockRegistry
    protected val logger: PlayerSkillsLogger = mockLogger

    override protected def restrictString(
      targetName: String,
      builder: TestConditionBuilder,
    ): Unit = {
      mockRestrictString(targetName, builder)
    }

    override protected def restrictOne(
      targetName: ResourceFacade,
      builder: TestConditionBuilder,
    ): Unit = {
      mockRestrictOne(targetName, builder)
    }
  }

  private val testUnit = TestRestrictionBuilder()

  "RestrictionBuilder.add" should "produce restrictions for a namespace" in {
    val restrictionName = "skillstest"
    val namespace = s"@$restrictionName"
    val conditionBuilder = TestConditionBuilder()
    val targetName = ResourceFacade("skillstest", "item").get

    mockRegistry.matchingNamespace(restrictionName) returns List(targetName)
    testUnit.add(namespace, conditionBuilder)

    testUnit.restrictions(namespace) shouldBe conditionBuilder
    testUnit.commit()

    mockLogger.info(*) wasCalled once
    mockRegistry.matchingNamespace(restrictionName) wasCalled once
    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a tag" in {
    val registryName = new ResourceLocation("skillstest", "registry")
    val mockKey = ResourceKey.createRegistryKey[Block](registryName)
    mockRegistry.name returns mockKey

    val restrictionName = "skillstest:tag"
    val tag = s"#$restrictionName"
    val conditionBuilder = TestConditionBuilder()
    val targetName = ResourceFacade("skillstest", "item").get

    mockRegistry.matchingTag(*) returns List(targetName)
    testUnit.add(tag, conditionBuilder)

    testUnit.restrictions(tag) shouldBe conditionBuilder
    testUnit.commit()

    mockLogger.info(*) wasCalled once
    mockRegistry.matchingTag(*) wasCalled once
    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a single resource" in {
    val restrictionName = "skillstest:item"
    val conditionBuilder = TestConditionBuilder()
    val targetName = ResourceFacade("skillstest", "item").get

    testUnit.add(restrictionName, conditionBuilder)

    testUnit.restrictions(restrictionName) shouldBe conditionBuilder
    testUnit.commit()

    mockRestrictOne(targetName, conditionBuilder) wasCalled once
  }

  it should "produce restrictions for a single string" in {
    val testUnit = TestRestrictionBuilder(true)
    val restrictionName = "skillstest:item"
    val conditionBuilder = TestConditionBuilder()

    testUnit.add(restrictionName, conditionBuilder)

    testUnit.restrictions(restrictionName) shouldBe conditionBuilder
    testUnit.commit()

    mockRestrictString(restrictionName, conditionBuilder) wasCalled once
  }

  "RestrictionBuilder.logRestriction" should "log restriction metadata" in {
    val restrictionName = "skillstest"
    val restriction = mock[Restriction[BlockFacade]]
    restriction.includeBiomes returns Seq.empty
    restriction.excludeBiomes returns Seq.empty
    restriction.includeDimensions returns Seq.empty
    restriction.excludeDimensions returns Seq.empty
    testUnit.logRestriction(restrictionName, restriction)

    mockLogger.info(*) wasCalled once
  }
}
