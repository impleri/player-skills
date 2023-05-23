package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.PlayerConditions

@ZenRegister
@NativeTypeRegistration(
  value = PlayerConditions::class,
  zenCodeName = "mods.playerskills.restrictions.PlayerConditions",
)
class ZenPlayerConditions
