package net.impleri.playerskills.api.skills

import net.impleri.playerskills.BaseSpec
import net.minecraft.resources.ResourceLocation

class ChangeableSkillSpec extends BaseSpec {
  private case class TestSkill(
    override val changesAllowed: Int = ChangeableSkill.UNLIMITED_CHANGES,
    override val options: List[String] = List.empty,
    override val value: Option[String] = None,
  ) extends ChangeableSkill[String] with ChangeableSkillOps[String, TestSkill] {
    override protected[playerskills] def mutate(
      value: Option[String],
      changesAllowed: Int
    ): TestSkill = copy(value = value, changesAllowed = changesAllowed)
  }

  private val testValue = "testvalue"
  private val secondValue = "second"
  private val disallowedValue = "nope"


  private val unlimited = TestSkill()
  private val restricted = TestSkill(ChangeableSkill.UNLIMITED_CHANGES, List(testValue, secondValue))
  private val oneChange = TestSkill(1, value = Some(secondValue))
  private val noChanges = TestSkill(0)

  "ChangeableSkill.areChangesAllowed" should "allow changes by default" in {
    unlimited.areChangesAllowed() should be (true)
  }

  it should "allow changes if more than 0 changes remain" in {
    oneChange.areChangesAllowed() should be (true)
  }

  it should "disallow changes if 0 changes remain" in {
    noChanges.areChangesAllowed() should be (false)
  }

  "ChangeableSkill.isAllowedValue" should "allow values if no options exist" in {
    unlimited.isAllowedValue(Some(testValue)) should be (true)
  }

  it should "allow values on the list of allowed options" in {
    restricted.isAllowedValue(Some(testValue)) should be (true)
  }

  it should "allow unsetting values despite the list of allowed options" in {
    restricted.isAllowedValue(None) should be (true)
  }

  it should "disallow values not on the list of allowed options" in {
    restricted.isAllowedValue(Some(disallowedValue)) should be (false)
  }

  "ChangeableSkillOps.mutate" should "return a new skill with the specified changes" in {
    val newChanges = 4
    val received = oneChange.mutate(None, newChanges)

    received.changesAllowed should be (newChanges)
    received.value shouldNot be (Some(secondValue))
    received.value should be (None)
  }

  it should "return a new skill with the change reduced automatically" in {
    val received = oneChange.mutate(Some(disallowedValue))

    received.changesAllowed should be(0)
    received.value should be (Some(disallowedValue))
  }
}
