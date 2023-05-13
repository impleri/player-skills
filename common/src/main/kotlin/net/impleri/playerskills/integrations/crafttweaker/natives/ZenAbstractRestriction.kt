package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.restrictions.AbstractRestriction

@ZenRegister
@NativeTypeRegistration(
  value = AbstractRestriction::class,
  zenCodeName = "mods.playerskills.restrictions.AbstractRestriction",
)
class ZenAbstractRestriction
