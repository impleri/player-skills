package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.kubejs.BuilderBase
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.impleri.playerskills.integrations.kubejs.Registries
import net.minecraft.resources.ResourceLocation
import java.util.Arrays

abstract class GenericSkillBuilderJS<T>(name: ResourceLocation?) : BuilderBase<Skill<T>?>(name) {
  var initialValue: T? = null
  var description: String? = null
  var options: List<T> = ArrayList()
  var changesAllowed = Skill.UNLIMITED_CHANGES
  var teamMode = TeamMode.off()
  var notifyKey: String? = null
  var notify = false
  
  override fun getRegistryType(): RegistryObjectBuilderTypes<in Skill<T>?>? {
    return Registries.SKILLS
  }

  @HideFromJS
  fun syncWith(skill: Skill<T>) {
    initialValue = skill.value
    description = skill.description
  }

  fun initialValue(value: T): GenericSkillBuilderJS<T> {
    initialValue = value
    return this
  }

  fun description(value: String?): GenericSkillBuilderJS<T> {
    description = value
    return this
  }

  fun options(options: Array<T>?): GenericSkillBuilderJS<T> {
    this.options = Arrays.stream(options).toList()
    return this
  }

  fun limitChanges(changesAllowed: Double): GenericSkillBuilderJS<T> {
    this.changesAllowed = changesAllowed.toInt()
    return this
  }

  fun unlimitedChanges(changesAllowed: Double?): GenericSkillBuilderJS<T> {
    this.changesAllowed = Skill.UNLIMITED_CHANGES
    return this
  }

  fun notifyOnChange(key: String?): GenericSkillBuilderJS<T> {
    notify = true
    if (key != null && !key.isEmpty()) {
      notifyKey = key
    }
    return this
  }

  fun notifyOnChange(): GenericSkillBuilderJS<T> {
    return notifyOnChange(null)
  }

  fun clearNotification(): GenericSkillBuilderJS<T> {
    notify = false
    notifyKey = null
    return this
  }

  fun clearValue(): GenericSkillBuilderJS<T> {
    initialValue = null
    return this
  }

  fun sharedWithTeam(): GenericSkillBuilderJS<T> {
    teamMode = TeamMode.shared()
    return this
  }

  fun teamLimitedTo(amount: Double?): GenericSkillBuilderJS<T> {
    teamMode = TeamMode.limited(amount!!)
    return this
  }

  fun percentageOfTeam(percentage: Double?): GenericSkillBuilderJS<T> {
    teamMode = TeamMode.proportional(percentage!!)
    return this
  }
}
