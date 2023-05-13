package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.minecraft.resources.ResourceLocation
import org.openzen.zencode.java.ZenCodeType

@ZenRegister
@ZenCodeType.Name("mods.playerskills.skills.SkillBuilder")
class SkillBuilder<T>(val name: ResourceLocation, val onSave: (SkillBuilder<T>) -> Boolean, skill: Skill<T>? = null) {
  var initialValue: T? = null
  var description: String? = null
  var options: List<T> = ArrayList()
  var changesAllowed = Skill.UNLIMITED_CHANGES
  var teamMode = TeamMode.off()
  var notifyKey: String? = null
  var notify = false

  init {
    PlayerSkills.LOGGER.info("Creating skill builder for $name")
    skill?.let {
      initialValue = it.value
      description = it.description
      options = it.options
      changesAllowed = it.changesAllowed
      teamMode = it.teamMode
      notify = it.notify
      notifyKey = it.notifyKey
    }
  }

  @ZenCodeType.Method
  fun save(): Boolean {
    return onSave(this)
  }

  @ZenCodeType.Method
  fun initialValue(value: T): SkillBuilder<T> {
    initialValue = value
    return this
  }

  @ZenCodeType.Method
  fun description(value: String?): SkillBuilder<T> {
    description = value
    return this
  }

  @ZenCodeType.Method
  fun options(options: Array<T>): SkillBuilder<T> {
    this.options = options.toList()
    return this
  }

  @ZenCodeType.Method
  fun limitChanges(changesAllowed: Double): SkillBuilder<T> {
    this.changesAllowed = changesAllowed.toInt()
    return this
  }

  @ZenCodeType.Method
  fun unlimitedChanges(): SkillBuilder<T> {
    this.changesAllowed = Skill.UNLIMITED_CHANGES
    return this
  }

  @ZenCodeType.Method
  fun notifyOnChange(key: String?): SkillBuilder<T> {
    notify = true

    if (!key.isNullOrBlank()) {
      notifyKey = key
    }

    return this
  }

  @ZenCodeType.Method
  fun notifyOnChange(): SkillBuilder<T> {
    return notifyOnChange(null)
  }

  @ZenCodeType.Method
  fun clearNotification(): SkillBuilder<T> {
    notify = false
    notifyKey = null
    return this
  }

  @ZenCodeType.Method
  fun clearValue(): SkillBuilder<T> {
    initialValue = null
    return this
  }

  @ZenCodeType.Method
  fun sharedWithTeam(): SkillBuilder<T> {
    teamMode = TeamMode.shared()
    return this
  }

  @ZenCodeType.Method
  fun teamLimitedTo(amount: Double): SkillBuilder<T> {
    teamMode = TeamMode.limited(amount)
    return this
  }

  @ZenCodeType.Method
  fun percentageOfTeam(percentage: Double): SkillBuilder<T> {
    teamMode = TeamMode.proportional(percentage)
    return this
  }

  @ZenCodeType.Method
  fun splitEvenlyAcrossTeam(): SkillBuilder<T> {
    teamMode = TeamMode.splitEvenly()
    return this
  }

  @ZenCodeType.Method
  fun pyramid(): SkillBuilder<T> {
    teamMode = TeamMode.pyramid()
    return this
  }
}
