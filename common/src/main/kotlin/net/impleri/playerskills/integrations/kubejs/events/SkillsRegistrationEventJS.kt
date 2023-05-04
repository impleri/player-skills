package net.impleri.playerskills.integrations.kubejs.events

import dev.architectury.registry.registries.DeferredRegister
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import dev.latvian.mods.kubejs.util.ConsoleJS
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.integrations.kubejs.skills.GenericSkillBuilderJS
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceKey

class SkillsRegistrationEventJS(types: Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<*>>>) :
  BaseSkillsRegistryEventJS(types) {
  /**
   * Add a new skill to the registry
   */
  @JvmOverloads
  fun <T> add(skillName: String, type: String, consumer: ((GenericSkillBuilderJS<T>) -> Unit)? = null): Boolean {
    val name = SkillResourceLocation.of(skillName)

    val builder = getBuilder<T>(type, name) ?: return false
    consumer?.let { it(builder) }

    val newSkill = builder.createObject()
    SKILLS.register(name) { newSkill }

    ConsoleJS.STARTUP.info("Created $type skill $name")
    return true
  }

  override fun afterPosted(isCanceled: Boolean) {
    SKILLS.register()
  }

  companion object {
    private val SKILL_REGISTRY = ResourceKey.createRegistryKey<Skill<*>>(Skill.REGISTRY_KEY)
    private val SKILLS = DeferredRegister.create(PlayerSkills.MOD_ID, SKILL_REGISTRY)
  }
}
