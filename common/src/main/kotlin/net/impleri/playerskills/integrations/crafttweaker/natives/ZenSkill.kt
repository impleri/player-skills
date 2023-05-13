package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.api.Skill

@ZenRegister
@NativeTypeRegistration(value = Skill::class, zenCodeName = "mods.playerskills.skills.Skill")
class ZenSkill
