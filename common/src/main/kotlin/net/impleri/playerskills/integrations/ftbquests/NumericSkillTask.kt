package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.TeamData
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import kotlin.math.roundToLong

class NumericSkillTask(quest: Quest?) : BasicSkillTask(quest) {
  var value = 1.0
  override val skillType: ResourceLocation
    get() = NumericSkillType.NAME

  override fun getType(): TaskType {
    return SkillTaskTypes.NUMERIC_SKILL
  }

  override fun getMaxProgress(): Long {
    return value.roundToLong()
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)
    nbt.putDouble("value", value)
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    value = nbt.getDouble("value")
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeDouble(value)
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    value = buffer.readDouble()
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)
    config.addDouble("value", value, { value = it }, 1.0, 0.0, Double.MAX_VALUE)
      .setNameKey("playerskills.quests.ui.value")
  }

  override fun canSubmit(teamData: TeamData, player: ServerPlayer): Boolean {
    return skill?.let { Player.can(player, it, value) } ?: false
  }
}
