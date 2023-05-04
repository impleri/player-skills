package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.TeamData
import dev.ftb.mods.ftbquests.quest.task.BooleanTask
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

open class BasicSkillTask(quest: Quest?) : BooleanTask(quest) {
  var skill: ResourceLocation? = null
  protected open val skillType: ResourceLocation
    get() = BasicSkillType.NAME

  override fun getType(): TaskType {
    return SkillTaskTypes.BASIC_SKILL
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)
    nbt.putString("skill", skill.toString())
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    skill = SkillResourceLocation.of(nbt.getString("skill"))
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeUtf(skill.toString(), Short.MAX_VALUE.toInt())
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    skill = SkillResourceLocation.of(buffer.readUtf(Short.MAX_VALUE.toInt()))
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)

    val skills = PlayerSkillsIntegration.getSkills(skillType)

    val firstSkill = skills[0]
    skill = skill ?: firstSkill

    config.addEnum(
      "skill",
      skill,
      { skill = it },
      NameMap.of(firstSkill, skills).create(),
      firstSkill
    ).setNameKey("playerskills.quests.ui.skill")
  }

  @Environment(EnvType.CLIENT)
  override fun getAltTitle(): MutableComponent {
    return Component.translatable("playerskills.quests.ui.skill").append(": ").append(
      Component.literal(skill.toString()).withStyle(ChatFormatting.YELLOW)
    )
  }

  override fun autoSubmitOnPlayerTick(): Int {
    return 20
  }

  override fun canSubmit(teamData: TeamData, player: ServerPlayer): Boolean {
    return skill?.let { Player.can<Boolean>(player, it) } ?: false
  }
}
