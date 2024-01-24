package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

class RestrictionRegistrySpec extends BaseSpec {
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

  "RestrictionRegistry.apply" should "return the correct class" in {
    val target = RestrictionRegistry(RestrictionRegistryState.empty)

    target.entries.isEmpty shouldBe true
  }

  "RestrictionRegistry.has" should "return true if there is a restriction with the given name" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)
    val testRestriction = TestRestriction()

    val (state, _) = RestrictionRegistryState.add(testRestriction).run(RestrictionRegistryState.empty).value

    val target = RestrictionRegistry(state)

    target.has(RestrictionType.Item(), testName) shouldBe true
  }

  it should "return false if there is no restriction with the given name" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)
    val testRestriction = TestRestriction()

    val (state, _) = RestrictionRegistryState.add(testRestriction).run(RestrictionRegistryState.empty).value

    val target = RestrictionRegistry(state)

    target.has(RestrictionType.Item(), ResourceLocation("skillstest", "other").get) shouldBe false
  }

  "RestrictionRegistry.get" should "return a view if there is a restriction with the given name" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)
    val testRestriction = TestRestriction()

    val (state, _) = RestrictionRegistryState.add(testRestriction).run(RestrictionRegistryState.empty).value

    val target = RestrictionRegistry(state)

    target.get(RestrictionType.Item(), testName).head shouldBe testRestriction
  }

  it should "return an empty view if there is no restriction with the given name" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)
    val testRestriction = TestRestriction()

    val (state, _) = RestrictionRegistryState.add(testRestriction).run(RestrictionRegistryState.empty).value

    val target = RestrictionRegistry(state)

    target.get(RestrictionType.Item(), ResourceLocation("skillstest", "other").get).isEmpty shouldBe true
  }

  "RestrictionRegistry.add" should "adds a restriction to the state" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)
    val testRestriction = TestRestriction()

    val target = RestrictionRegistry(RestrictionRegistryState.empty)

    target.add(testRestriction) shouldBe true
    target.entries.head shouldBe testRestriction
  }
}
