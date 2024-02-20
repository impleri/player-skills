package net.impleri.playerskills.data.conditions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation

class RecipeRestrictionConditionBuilderSpec extends BaseSpec {
  private val targetName = new ResourceLocation("skillstest", "condition")
  private val mockSkillOps = mock[SkillOps]
  private val mockSkillTypeOps = mock[SkillTypeOps]
  private val mockPlayerOps = mock[PlayerOps]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = RecipeRestrictionConditionBuilder(
    targetName,
    mockSkillOps,
    mockSkillTypeOps,
    mockPlayerOps,
    mockLogger,
  )

  private val mockJson = mock[JsonObject]

  "RecipeRestrictionConditionBuilder.toggleEverything" should "allow everything" in {
    testUnit.isProducible shouldBe None

    testUnit.toggleEverything()

    testUnit.isProducible.value shouldBe true
  }

  "RecipeRestrictionConditionBuilder.toggleNothing" should "deny everything" in {
    testUnit.isProducible shouldBe None

    testUnit.toggleNothing()

    testUnit.isProducible.value shouldBe false
  }

  "RecipeRestrictionConditionBuilder.parseRestriction" should "parse values from JSON elements" in {
    val producible = true

    val mockRecipeData = mock[JsonObject]
    val mockRecipeElement = mock[JsonElement]
    mockRecipeElement.isJsonObject returns true
    mockRecipeElement.getAsJsonObject returns mockRecipeData
    mockJson.get("recipe") returns mockRecipeData

    val givenType = "smelting"
    mockRecipeData.get("type") returns new JsonPrimitive(givenType)

    mockJson.get("producible") returns new JsonPrimitive(producible)

    testUnit.parseRestriction(mockJson)

    testUnit.isProducible.value shouldBe true
  }
}
