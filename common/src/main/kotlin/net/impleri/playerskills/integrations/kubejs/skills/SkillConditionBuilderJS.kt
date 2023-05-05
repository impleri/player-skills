package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.kubejs.BuilderBase
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import dev.latvian.mods.rhino.util.HideFromJS
import dev.latvian.mods.rhino.util.RemapForJS
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.integrations.kubejs.Registries
import net.minecraft.util.RandomSource

// @TODO: Move this into API
class SkillConditionBuilderJS<T>(private var skill: Skill<T>) :
  BuilderBase<Skill<T>>(
    skill.name,
  ) {
  private val type: SkillType<T>? = SkillType.find(skill)
  private val random = RandomSource.create()

  var appliedChance = 100.0
  var min: T? = null
  var max: T? = null
  var conditionIf: Boolean? = null
  var conditionUnless: Boolean? = null

  override fun getRegistryType(): RegistryObjectBuilderTypes<Skill<*>> {
    return Registries.SKILLS
  }

  override fun createObject(): Skill<T>? {
    return null
  }

  @HideFromJS
  private fun calculateConditions(): Boolean {
    // True if there is no if condition or if the condition is true
    val hasIf = conditionIf ?: true

    // True if there is no unless condition or if the condition is false
    val hasUnless = conditionUnless?.let { !it } ?: true

    val hasChance = appliedChance >= random.nextIntBetweenInclusive(0, 100)

    PlayerSkills.LOGGER.debug(
      "Checking conditions. IF: $conditionIf->$hasIf. UNLESS: $conditionUnless->$hasUnless, CHANCE: $appliedChance->$hasChance",
    )

    return hasIf && hasUnless && hasChance
  }

  @HideFromJS
  fun shouldChange(): Boolean {
    return type != null && calculateConditions()
  }

  @HideFromJS
  fun calculatePrev(): T? {
    return if (shouldChange()) {
      type!!.getPrevValue(skill, min, max)
    } else {
      null
    }
  }

  @HideFromJS
  fun calculateNext(): T? {
    return if (shouldChange()) {
      type!!.getNextValue(skill, min, max)
    } else {
      null
    }
  }

  fun chance(value: Double): SkillConditionBuilderJS<T> {
    appliedChance = value
    return this
  }

  fun min(value: T): SkillConditionBuilderJS<T> {
    min = value
    return this
  }

  fun max(value: T): SkillConditionBuilderJS<T> {
    max = value
    return this
  }

  @RemapForJS("if")
  fun onlyIf(value: Boolean): SkillConditionBuilderJS<T> {
    conditionIf = value
    return this
  }

  fun unless(value: Boolean): SkillConditionBuilderJS<T> {
    conditionUnless = value
    return this
  }
}
