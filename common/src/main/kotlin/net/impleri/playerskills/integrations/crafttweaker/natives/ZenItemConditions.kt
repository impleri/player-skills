package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.items.ItemConditions

@ZenRegister
@NativeTypeRegistration(
  value = ItemConditions::class,
  zenCodeName = "mods.playerskills.ItemConditions",
)
class ZenItemConditions
