package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.BiomeConditions

@ZenRegister
@NativeTypeRegistration(
  value = BiomeConditions::class,
  zenCodeName = "mods.playerskills.restrictions.BiomeConditions",
)
class ZenBiomeConditions
