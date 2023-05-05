package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

open class NumericSkillReward(quest: Quest?) : BasicSkillReward(quest) {
  var min = NO_VALUE
  var max = NO_VALUE
  override val skillType: ResourceLocation
    get() = NumericSkillType.NAME

  override fun getType(): RewardType {
    return SkillRewardTypes.NUMERIC_SKILL
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)
    nbt.putDouble("min", min)
    nbt.putDouble("max", max)
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    min = nbt.getDouble("min")
    max = nbt.getDouble("max")
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeDouble(min)
    buffer.writeDouble(max)
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    min = buffer.readDouble()
    max = buffer.readDouble()
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)
    config.addDouble("min", min, { min = it }, NO_VALUE, NO_VALUE, Double.MAX_VALUE)
      .setNameKey("playerskills.quests.ui.min")
    config.addDouble("max", max, { max = it }, NO_VALUE, NO_VALUE, Double.MAX_VALUE)
      .setNameKey("playerskills.quests.ui.max")
  }

  override fun claim(player: ServerPlayer, notify: Boolean) {
    val actualSkill = skill?.let { Player.get<Double>(player, it) } ?: throw RuntimeException()
    val skillType = actualSkill.let { SkillType.find<Double>(skillType) } ?: throw RuntimeException()

    val nextVal = if (downgrade) {
      skillType.getPrevValue(
        actualSkill,
        if (min < 0) null else min,
        if (max < 0) null else max,
      )
    } else {
      skillType.getNextValue(actualSkill, if (min < 0) null else min, if (max < 0) null else max)
    }

    if (nextVal != null && Player.set(player, actualSkill, nextVal)) {
      maybeNotify(player, notify, nextVal.toString())
    }
  }

  companion object {
    protected const val NO_VALUE = -1.0
  }
}
