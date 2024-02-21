package net.impleri.playerskills.data.conditions

import com.google.gson.JsonArray
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

    val ingredient1 = "minecraft:stone"
    val ingredient2 = "minecraft:cobblestone"
    val ingredients = new JsonArray()
    ingredients.add(new JsonPrimitive(ingredient1))
    ingredients.add(new JsonPrimitive(ingredient2))

    val givenType = "minecraft:smelting"
    val givenOutput = "minecraft:andesite"

    val recipeData = new JsonObject()
    recipeData.addProperty("type", givenType)
    recipeData.addProperty("output", givenOutput)
    recipeData.add("ingredients", ingredients)

    val recipeRestriction = new JsonObject()
    recipeRestriction.add("recipe", recipeData)
    recipeRestriction.addProperty("producible", producible)

    testUnit.parseRestriction(recipeRestriction)

    testUnit.targets.head.recipeType.toString shouldBe givenType
    testUnit.targets.head.output.value shouldBe givenOutput
    testUnit.targets.head.ingredients shouldBe Seq(ingredient1, ingredient2)
    testUnit.isProducible.value shouldBe true
  }
}
