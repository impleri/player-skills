package net.impleri.playerskills.integrations.kubejs

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.utils.SkillResourceLocation.of
import net.minecraft.resources.ResourceKey

object Registries {
  private val key = ResourceKey.createRegistryKey<Skill<*>>(of("skill_builders_registry"))

  val SKILLS = RegistryObjectBuilderTypes.add(key, Skill::class.java)
}
