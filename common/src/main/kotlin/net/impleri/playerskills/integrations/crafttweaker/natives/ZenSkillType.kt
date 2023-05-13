package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.api.SkillType

@ZenRegister
@NativeTypeRegistration(value = SkillType::class, zenCodeName = "mods.playerskills.skills.SkillType")
class ZenSkillType
