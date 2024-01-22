package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.crafting.Recipe
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Biome
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.player.{Player => MinecraftPlayer}
import net.minecraft.world.Container

class RestrictionOpsSpec extends BaseSpec {
  private val mockRegistry = mock[RestrictionRegistry]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testType: RestrictionType = RestrictionType.Recipe()

  private case class TestOps() extends RestrictionsOps[Recipe[Container], Restriction[Recipe[Container]]] {
    override def restrictionType: RestrictionType = testType

    override def registry: RestrictionRegistry = mockRegistry

    override def logger: PlayerSkillsLogger = mockLogger
  }

  private val mockPlayer = mock[Player[MinecraftPlayer]]
  private val mockTargetName = mock[ResourceLocation]
  private val mockRestriction = mock[Restriction[Recipe[Container]]]
  private val mockTarget = mock[Recipe[Container]]

  private val testUnit = TestOps()

  "Restriction.matchesPlayer" should "return true if the condition passes" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition
    testUnit.matchesPlayer(mockPlayer)(mockRestriction) should be(true)
  }

  "Restriction.matchesTarget" should "return true if the type and target match" in {
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true
    testUnit.matchesTarget(mockTargetName)(mockRestriction) should be(true)
  }

  it should "return false if the type does not match" in {
    mockRestriction.isType(testType) returns false
    mockRestriction.targets(mockTargetName) returns true
    testUnit.matchesTarget(mockTargetName)(mockRestriction) should be(false)
  }

  it should "return false if the target does not match" in {
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns false
    testUnit.matchesTarget(mockTargetName)(mockRestriction) should be(false)
  }

  "Restriction.getRestrictionsFor" should "return only restrictions that match the parameters" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    val testDimension = mock[ResourceLocation]
    mockRestriction.isAllowedDimension(testDimension) returns true
    val testBiome = mock[Biome]
    mockRestriction.isAllowedBiome(testBiome) returns true

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, Option(testDimension), Option(testBiome))

    returnedView.size shouldBe (1)
    returnedView.toList contains (mockRestriction)
  }

  it should "return restrictions even if the biome and dimension are empty" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    mockRestriction.isAllowedDimension(*) wasNever called
    mockRestriction.isAllowedBiome(*) wasNever called

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, None, None)

    returnedView.size shouldBe (1)
    returnedView.toList contains (mockRestriction)
  }

  it should "return an empty list if the condition does not match" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns false
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    val testDimension = mock[ResourceLocation]
    mockRestriction.isAllowedDimension(testDimension) returns true
    val testBiome = mock[Biome]
    mockRestriction.isAllowedBiome(testBiome) returns true

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, Option(testDimension), Option(testBiome))

    returnedView.isEmpty shouldBe (true)
  }

  it should "return an empty list if the target does not match" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns false

    val testDimension = mock[ResourceLocation]
    mockRestriction.isAllowedDimension(testDimension) returns true
    val testBiome = mock[Biome]
    mockRestriction.isAllowedBiome(testBiome) returns true

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, Option(testDimension), Option(testBiome))

    returnedView.isEmpty shouldBe (true)
  }

  it should "return an empty list if the dimension does not match" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    val testDimension = mock[ResourceLocation]
    mockRestriction.isAllowedDimension(testDimension) returns false

    val testBiome = mock[Biome]
    mockRestriction.isAllowedBiome(testBiome) returns true

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, Option(testDimension), Option(testBiome))

    returnedView.isEmpty shouldBe (true)
  }

  it should "return an empty list if the biome does not match" in {
    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true
    mockRestriction.condition returns mockCondition

    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    val testDimension = mock[ResourceLocation]
    mockRestriction.isAllowedDimension(testDimension) returns true

    val testBiome = mock[Biome]
    mockRestriction.isAllowedBiome(testBiome) returns false

    mockRegistry.entries returns List(mockRestriction)

    val returnedView = testUnit.getRestrictionsFor(mockPlayer, mockTargetName, Option(testDimension), Option(testBiome))

    returnedView.isEmpty shouldBe (true)
  }

  "Restriction.canPlayer" should "return true if no restriction matches all parameters" in {
    val mockEntity = mock[Entity[MinecraftPlayer]]
    mockEntity.asPlayer[MinecraftPlayer] returns mockPlayer
    mockPlayer.asOption returns Option(mockEntity)
    mockPlayer.dimension returns None
    mockPlayer.biomeAt(None) returns None

    mockTarget.getName returns Option(mockTargetName)

    val fieldGetter = mock[Restriction[Recipe[Container]] => Boolean]
    fieldGetter(*) returns true

    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true

    mockRestriction.condition returns mockCondition
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    mockRegistry.entries returns List(mockRestriction)

    val playerCan = testUnit
      .canPlayer(mockPlayer, mockTarget, fieldGetter, "testField")

    playerCan shouldBe true

    mockLogger.debug(*) wasCalled once
    mockLogger.warn(*) wasNever called
  }

  it should "return false if any restriction matches all parameters" in {
    val mockEntity = mock[Entity[MinecraftPlayer]]
    mockEntity.asPlayer[MinecraftPlayer] returns mockPlayer
    mockPlayer.asOption returns Option(mockEntity)
    mockPlayer.dimension returns None
    mockPlayer.biomeAt(None) returns None

    mockTarget.getName returns Option(mockTargetName)

    val fieldGetter = mock[Restriction[Recipe[Container]] => Boolean]
    fieldGetter(*) returns false

    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true

    mockRestriction.condition returns mockCondition
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true

    mockRegistry.entries returns List(mockRestriction)

    val playerCan = testUnit
      .canPlayer(mockPlayer, mockTarget, fieldGetter, "testField")

    playerCan shouldBe false

    mockLogger.debug(*) wasCalled once
    mockLogger.warn(*) wasNever called
  }

  it should "return default value if there is no player" in {
    mockPlayer.asOption returns None

    mockTarget.getName returns Option(mockTargetName)

    val fieldGetter = mock[Restriction[Recipe[Container]] => Boolean]

    val playerCan = testUnit
      .canPlayer(mockPlayer, mockTarget, fieldGetter, "testField")

    playerCan shouldBe RestrictionsOps.DEFAULT_RESPONSE

    mockLogger.debug(*) wasNever called
    mockLogger.warn(*) wasCalled once
  }

  it should "return default value if there is no target" in {
    val mockEntity = mock[Entity[MinecraftPlayer]]
    mockEntity.asPlayer[MinecraftPlayer] returns mockPlayer
    mockPlayer.asOption returns Option(mockEntity)
    mockPlayer.name returns "player"

    mockTarget.getName returns None

    val fieldGetter = mock[Restriction[Recipe[Container]] => Boolean]

    val playerCan = testUnit
      .canPlayer(mockPlayer, mockTarget, fieldGetter, "testField")

    playerCan shouldBe RestrictionsOps.DEFAULT_RESPONSE

    mockLogger.debug(*) wasNever called
    mockLogger.warn(*) wasCalled once
  }

  "Restriction.getReplacementFor" should "return the first replacement found" in {
    val mockEntity = mock[Entity[MinecraftPlayer]]
    mockEntity.asPlayer[MinecraftPlayer] returns mockPlayer
    mockPlayer.asOption returns Option(mockEntity)
    mockPlayer.dimension returns None
    mockPlayer.biomeAt(None) returns None

    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true

    val mockReplacement = mock[Recipe[Container]]

    mockRestriction.condition returns mockCondition
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true
    mockRestriction.hasReplacement returns true
    mockRestriction.replacement returns Option(mockReplacement)

    mockRegistry.entries returns List(mockRestriction)

    val replacement = testUnit
      .getReplacementFor(mockPlayer, mockTargetName)

    replacement.value shouldBe mockReplacement

    mockLogger.debug(*) wasCalled once
  }

  it should "return None if no replacement found" in {
    val mockEntity = mock[Entity[MinecraftPlayer]]
    mockEntity.asPlayer[MinecraftPlayer] returns mockPlayer
    mockPlayer.asOption returns Option(mockEntity)
    mockPlayer.dimension returns None
    mockPlayer.biomeAt(None) returns None

    val mockCondition = mock[Player[_] => Boolean]
    mockCondition(*) returns true

    val mockReplacement = mock[Recipe[Container]]

    mockRestriction.condition returns mockCondition
    mockRestriction.isType(testType) returns true
    mockRestriction.targets(mockTargetName) returns true
    mockRestriction.hasReplacement returns false

    mockRegistry.entries returns List(mockRestriction)

    val replacement = testUnit
      .getReplacementFor(mockPlayer, mockTargetName)

    replacement shouldBe None

    mockLogger.debug(*) wasCalled once
  }
}
