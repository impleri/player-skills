package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder

@ZenRegister
@NativeTypeRegistration(
  value = RestrictionConditionsBuilder::class,
  zenCodeName = "mods.playerskills.restrictions.RestrictionConditionsBuilder",
)
class ZenRestrictionConditionsBuilder
