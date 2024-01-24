package net.impleri.playerskills.restrictions

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionType
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation

class RestrictionRegistryStateSpec extends BaseSpec {
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

  "RestrictionRegistryStateSpec.empty" should "create an empty state" in {
    val state = RestrictionRegistryState.empty

    RestrictionRegistryState.entries().run(state).value._2.isEmpty shouldBe true
  }

  "RestrictionRegistryStateSpec.add" should "return a new state with the added skill" in {
    val initialState = RestrictionRegistryState.empty
    val testRestriction = TestRestriction()

    val (nextState, _) = RestrictionRegistryState.add(testRestriction).run(initialState).value

    val entries = RestrictionRegistryState.entries().run(nextState).value._2

    entries.size shouldBe 1
    entries.head shouldBe testRestriction
  }

  "RestrictionRegistryStateSpec.has" should "return a new state with a view of matching restrictions" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)

    val initialState = RestrictionRegistryState.empty
    val testRestriction = TestRestriction()

    val (nextState, _) = RestrictionRegistryState.add(testRestriction).run(initialState).value

    RestrictionRegistryState.has(RestrictionType.Item(), testName).run(nextState).value._2 shouldBe true
  }

  "RestrictionRegistryStateSpec.get" should "return a new state with a view of matching restrictions" in {
    val testName = ResourceLocation("skillstest", "restriction").get
    mockItem.getName returns Option(testName)

    val initialState = RestrictionRegistryState.empty
    val testRestriction = TestRestriction()

    val (nextState, _) = RestrictionRegistryState.add(testRestriction).run(initialState).value

    RestrictionRegistryState.get(RestrictionType.Item(), testName).run(nextState).value._2.head shouldBe testRestriction
  }
}
