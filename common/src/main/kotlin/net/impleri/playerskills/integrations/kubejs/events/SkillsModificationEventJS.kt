package net.impleri.playerskills.integrations.kubejs.events

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import dev.latvian.mods.kubejs.util.ConsoleJS
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.integrations.kubejs.skills.GenericSkillBuilderJS
import net.impleri.playerskills.utils.SkillResourceLocation

class SkillsModificationEventJS(types: Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<*>>>) :
  BaseSkillsRegistryEventJS(types) {

  @JvmOverloads
  fun <T> modify(
    skillName: String,
    skillType: String? = null,
    consumer: ((GenericSkillBuilderJS<T>) -> Unit)? = null,
  ): Boolean {
    val name = SkillResourceLocation.of(skillName)
    val skill = Skill.find<T>(name) ?: return false

    val type = skillType ?: skill.type.toString()

    val builder = getBuilder<T>(type, name) ?: return false

    builder.syncWith(skill)
    consumer?.let { it(builder) }
    val newSkill = builder.createObject() ?: return false

    val response = Skill.modify(newSkill)
    ConsoleJS.SERVER.info("Updated $type skill $name")

    return response
  }

  fun <T> remove(name: String): Boolean {
    val skill = Skill.find<T>(name) ?: return true
    val response = Skill.remove(skill)

    ConsoleJS.SERVER.info("Removed skill $name")

    return response
  }
}
