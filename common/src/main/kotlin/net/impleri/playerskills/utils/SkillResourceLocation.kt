package net.impleri.playerskills.utils

import net.minecraft.resources.ResourceLocation

/**
 * Helper class for generating ResourceLocation, defaulting the namespace to playerSkills
 */
object SkillResourceLocation {
  @JvmStatic
  fun of(resourceLocation: ResourceLocation): ResourceLocation {
    return resourceLocation
  }

  @JvmStatic
  fun of(namespace: String, path: String): ResourceLocation {
    return ResourceLocation(namespace, path)
  }

  @JvmStatic
  fun of(path: String): ResourceLocation {
    val elements = decompose(path)
    return of(elements[0], elements[1])
  }

  /**
   * ofMinecraft
   *
   *
   * Attempt to parse the string into a minecraft-default ResourceLocation and fallback to skills namespace if some error
   */
  @JvmStatic
  fun ofMinecraft(value: String): ResourceLocation {
    val mcResource = ResourceLocation.tryParse(value)
    return mcResource ?: of(value)
  }

  private const val DEFAULT_NAMESPACE = "skills"
  private fun decompose(string: String): Array<String> {
    var namespace = DEFAULT_NAMESPACE
    var element = string
    val i = string.indexOf(":")
    if (i >= 0) {
      element = string.substring(i + 1)
      if (i >= 1) {
        namespace = string.substring(0, i)
      }
    }
    return arrayOf(namespace, element)
  }
}
