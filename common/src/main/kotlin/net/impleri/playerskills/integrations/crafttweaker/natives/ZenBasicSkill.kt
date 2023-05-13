package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.skills.basic.BasicSkill

@ZenRegister
@NativeTypeRegistration(value = BasicSkill::class, zenCodeName = "mods.playerskills.skills.BasicSkill")
class ZenBasicSkill
