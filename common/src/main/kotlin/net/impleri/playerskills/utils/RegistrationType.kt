package net.impleri.playerskills.utils

import net.impleri.playerskills.utils.SkillResourceLocation.ofMinecraft
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

class RegistrationType<T>(value: String, registryKey: ResourceKey<Registry<T>>) {
  private var namespace: String? = null
  private var name: ResourceLocation? = null
  private var tag: TagKey<T>? = null

  init {
    if (value.trim { it <= ' ' }.startsWith("@")) {
      namespace = value.substring(1)
      name = null
      tag = null
    } else if (value.trim { it <= ' ' }.startsWith("#")) {
      val tagKey = value.substring(1)
      tag = TagKey.create(registryKey, ofMinecraft(tagKey))
      namespace = null
      name = null
    } else if (value.trim { it <= ' ' }.endsWith(":*")) {
      namespace = value.substring(0, value.indexOf(":"))
      name = null
      tag = null
    } else {
      name = ofMinecraft(value)
      namespace = null
      tag = null
    }
  }

  fun ifTag(consumer: (TagKey<T>) -> Unit) {
    tag?.let { consumer(it) }
  }

  fun ifName(consumer: (ResourceLocation) -> Unit) {
    name?.let { consumer(it) }
  }

  fun ifNamespace(consumer: (String) -> Unit) {
    namespace?.let { consumer(it) }
  }
}
