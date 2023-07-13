package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.TeamMode
import net.impleri.playerskills.integrations.crafttweaker.skills.SkillBuilder
import net.impleri.playerskills.skills.basic.BasicSkill
import net.impleri.playerskills.skills.numeric.NumericSkill
import net.impleri.playerskills.skills.specialized.SpecializedSkill
import net.impleri.playerskills.skills.tiered.TieredSkill
import net.impleri.playerskills.utils.SkillResourceLocation
import org.openzen.zencode.java.ZenCodeType

@ZenRegister
@ZenCodeType.Name("mods.playerskills.Skills")
object Skills {
  @ZenCodeType.Method
  @JvmStatic
  fun basic(name: String): SkillBuilder<Boolean> {
    val skillName = SkillResourceLocation.of(name)
    val originalSkill = Skill.find<Boolean>(skillName)

    val onSave = { builder: SkillBuilder<Boolean> ->
      val skill = BasicSkill(
        builder.name,
        builder.initialValue,
        builder.description,
        builder.options,
        builder.changesAllowed,
        ensureValidTeamMode(builder.teamMode),
        builder.notify,
        builder.notifyKey,
      )

      Skill.modify(skill)
    }

    return SkillBuilder(skillName, onSave, originalSkill)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun numeric(name: String): SkillBuilder<Double> {
    val skillName = SkillResourceLocation.of(name)
    val originalSkill = Skill.find<Double>(skillName)

    val onSave = { builder: SkillBuilder<Double> ->
      val skill = NumericSkill(
        builder.name,
        builder.initialValue,
        builder.description,
        builder.options,
        builder.changesAllowed,
        ensureValidTeamMode(builder.teamMode),
        builder.notify,
        builder.notifyKey,
      )

      Skill.modify(skill)
    }

    return SkillBuilder(skillName, onSave, originalSkill)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun tiered(name: String): SkillBuilder<String> {
    val skillName = SkillResourceLocation.of(name)
    val originalSkill = Skill.find<String>(skillName)

    val onSave = { builder: SkillBuilder<String> ->
      val skill = TieredSkill(
        builder.name,
        builder.initialValue,
        builder.description,
        builder.options,
        builder.changesAllowed,
        ensureValidTeamMode(builder.teamMode, TeamMode.pyramid()),
        builder.notify,
        builder.notifyKey,
      )

      Skill.modify(skill)
    }

    return SkillBuilder(skillName, onSave, originalSkill)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun specialized(name: String): SkillBuilder<String> {
    val skillName = SkillResourceLocation.of(name)
    val originalSkill = Skill.find<String>(skillName)

    val onSave = { builder: SkillBuilder<String> ->
      val skill = SpecializedSkill(
        builder.name,
        builder.initialValue,
        builder.description,
        builder.options,
        builder.changesAllowed,
        ensureValidTeamMode(builder.teamMode, TeamMode.splitEvenly()),
        builder.notify,
        builder.notifyKey,
      )

      Skill.modify(skill)
    }

    return SkillBuilder(skillName, onSave, originalSkill)
  }

  @ZenCodeType.Method
  @JvmStatic
  fun remove(name: String): Boolean {
    return Skill.find<Any>(name)?.let { Skill.remove(it) } ?: false
  }

  private fun ensureValidTeamMode(mode: TeamMode, allowed: TeamMode? = null): TeamMode {
    return when {
      mode.isPyramid && allowed?.isPyramid != true -> TeamMode.off()
      mode.isSplitEvenly && allowed?.isSplitEvenly != true -> TeamMode.off()
      else -> mode
    }
  }
}
