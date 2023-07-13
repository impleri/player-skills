package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import dev.latvian.mods.kubejs.event.EventJS
import dev.latvian.mods.rhino.util.HideFromJS
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

open class BaseSkillsRegistryEventJS(private val types: Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<*>>>) :
  EventJS() {
  @HideFromJS
  protected fun <T> getBuilder(skillType: String, name: ResourceLocation): GenericSkillBuilderJS<T>? {
    // Ensure we have a full ResourceLocation before casting to String
    val type = SkillResourceLocation.of(skillType).toString()
    PlayerSkills.LOGGER.debug("Creating skill builder for $name typed as $type")

    val builderType = types[type] ?: return null

    val uncastBuilder = builderType.factory().createBuilder(name)

    return if (uncastBuilder is GenericSkillBuilderJS<*>) {
      @Suppress("UNCHECKED_CAST")
      uncastBuilder as GenericSkillBuilderJS<T>
    } else {
      null
    }
  }
}
