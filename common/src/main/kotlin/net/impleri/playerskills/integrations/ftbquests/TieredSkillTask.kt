package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.TeamData
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

open class TieredSkillTask(quest: Quest?) : BasicSkillTask(quest) {
  var value: String = NO_VALUE
  override val skillType: ResourceLocation
    get() = TieredSkillType.NAME

  override fun getType(): TaskType {
    return SkillTaskTypes.TIERED_SKILL
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)
    nbt.putString("value", value)
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    value = nbt.getString("value")
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeUtf(value, Short.MAX_VALUE.toInt())
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    value = buffer.readUtf(Short.MAX_VALUE.toInt())
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)
    val actualSkill = skill?.let { Skill.find<String>(it) } ?: throw RuntimeException()

    val options = actualSkill.options

    if (!actualSkill.isAllowedValue(value)) {
      value = options[0]
    }

    config.addEnum(
      "value",
      value,
      { value = it },
      NameMap.of(
        options[0],
        options,
      ).create(),
      actualSkill.value,
    )
      .setNameKey("playerskills.quests.ui.value")
  }

  override fun canSubmit(teamData: TeamData, player: ServerPlayer): Boolean {
    return skill?.let { Player.can(player, it, value) } ?: false
  }

  companion object {
    protected const val NO_VALUE = ""
  }
}
