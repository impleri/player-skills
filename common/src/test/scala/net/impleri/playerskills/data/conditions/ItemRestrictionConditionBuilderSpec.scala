package net.impleri.playerskills.data.conditions

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

class ItemRestrictionConditionBuilderSpec extends BaseSpec {
  private val targetName = new ResourceLocation("skillstest", "condition")
  private val mockSkillOps = mock[SkillOps]
  private val mockSkillTypeOps = mock[SkillTypeOps]
  private val mockPlayerOps = mock[PlayerOps]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = ItemRestrictionConditionBuilder(
    targetName,
    mockSkillOps,
    mockSkillTypeOps,
    mockPlayerOps,
    mockLogger,
  )

  private val mockJson = mock[JsonObject]

  "ItemRestrictionConditionBuilder.toggleEverything" should "allow everything" in {
    testUnit.isIdentifiable shouldBe None
    testUnit.isHoldable shouldBe None
    testUnit.isWearable shouldBe None
    testUnit.isUsable shouldBe None
    testUnit.isHarmful shouldBe None

    testUnit.toggleEverything()

    testUnit.isIdentifiable.value shouldBe true
    testUnit.isHoldable.value shouldBe true
    testUnit.isWearable.value shouldBe true
    testUnit.isUsable.value shouldBe true
    testUnit.isHarmful.value shouldBe true
  }

  "ItemRestrictionConditionBuilder.toggleNothing" should "deny everything" in {
    testUnit.isIdentifiable shouldBe None
    testUnit.isHoldable shouldBe None
    testUnit.isWearable shouldBe None
    testUnit.isUsable shouldBe None
    testUnit.isHarmful shouldBe None

    testUnit.toggleNothing()

    testUnit.isIdentifiable.value shouldBe false
    testUnit.isHoldable.value shouldBe false
    testUnit.isWearable.value shouldBe false
    testUnit.isUsable.value shouldBe false
    testUnit.isHarmful.value shouldBe false
  }

  "ItemRestrictionConditionBuilder.parseRestriction" should "parse values from JSON elements" in {
    val givenTarget = "targetName"
    val identifiable = true
    val holdable = "true"
    val wearable = false

    mockJson.get("item") returns new JsonPrimitive(givenTarget)
    mockJson.get("identifiable") returns new JsonPrimitive(identifiable)
    mockJson.get("holdable") returns new JsonPrimitive(holdable)
    mockJson.get("wearable") returns new JsonPrimitive(wearable)
    mockJson.get("usable") returns new JsonNull()
    mockJson.get("harmful") returns null

    testUnit.parseRestriction(mockJson)

    testUnit.target.value shouldBe givenTarget
    testUnit.isIdentifiable.value shouldBe true
    testUnit.isHoldable shouldBe None
    testUnit.isWearable.value shouldBe false
    testUnit.isUsable shouldBe None
    testUnit.isHarmful shouldBe None
  }
}
