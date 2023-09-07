package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.mobs.MobConditions

@ZenRegister
@NativeTypeRegistration(
  value = MobConditions::class,
  zenCodeName = "mods.playerskills.MobConditions",
)
class ZenMobConditions
