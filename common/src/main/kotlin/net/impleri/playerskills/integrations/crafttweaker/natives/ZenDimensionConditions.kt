package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.DimensionConditions

@ZenRegister
@NativeTypeRegistration(
  value = DimensionConditions::class,
  zenCodeName = "mods.playerskills.restrictions.DimensionConditions",
)
class ZenDimensionConditions
