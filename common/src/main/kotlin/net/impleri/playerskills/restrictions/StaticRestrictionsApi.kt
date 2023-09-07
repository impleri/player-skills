package net.impleri.playerskills.restrictions

import net.minecraft.resources.ResourceLocation

interface StaticRestrictionsApi<T, R : AbstractRestriction<T>> {
  fun add(name: ResourceLocation, restriction: R)

  fun getName(value: T?): ResourceLocation

  fun getValue(name: ResourceLocation): T?

  fun isDefaultValue(value: T?): Boolean

  fun isDefaultValue(value: ResourceLocation? = null): Boolean

  fun isDefaultValue(value: T?, target: ResourceLocation? = null): Boolean {
    return isDefaultValue(value) && !isDefaultValue(target)
  }
}
