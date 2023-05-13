package net.impleri.playerskills.integrations.crafttweaker.natives

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration
import net.impleri.playerskills.api.TeamMode

@ZenRegister
@NativeTypeRegistration(value = TeamMode::class, zenCodeName = "mods.playerskills.skills.TeamMode")
class ZenTeamMode
