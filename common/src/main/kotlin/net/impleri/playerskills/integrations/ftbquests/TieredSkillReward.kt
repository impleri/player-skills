package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

open class TieredSkillReward(quest: Quest?) : BasicSkillReward(quest) {
  var min: String = NO_VALUE
  var max: String = NO_VALUE
  override val skillType: ResourceLocation
    get() = TieredSkillType.NAME

  override fun getType(): RewardType {
    return SkillRewardTypes.TIERED_SKILL
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)
    nbt.putString("min", min)
    nbt.putString("max", max)
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    min = nbt.getString("min")
    max = nbt.getString("max")
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeUtf(min, Short.MAX_VALUE.toInt())
    buffer.writeUtf(max, Short.MAX_VALUE.toInt())
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    min = buffer.readUtf(Short.MAX_VALUE.toInt())
    max = buffer.readUtf(Short.MAX_VALUE.toInt())
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)

    val actualSkill = skill?.let { Skill.find<String>(it) } ?: return
    val options = actualSkill.options.toMutableList()
    options.add(0, NO_VALUE)

    if (!actualSkill.isAllowedValue(min)) {
      min = NO_VALUE
    }

    if (!actualSkill.isAllowedValue(max)) {
      max = NO_VALUE
    }

    config.addEnum("min", min, { min = it }, NameMap.of(NO_VALUE, options).create(), actualSkill.value)
      .setNameKey("playerskills.quests.ui.min")

    config.addEnum("max", max, { max = it }, NameMap.of(NO_VALUE, options).create(), actualSkill.value)
      .setNameKey("playerskills.quests.ui.min")
  }

  override fun claim(player: ServerPlayer, notify: Boolean) {
    val actualSkill = skill?.let { Player.get<String>(player, it) } ?: throw RuntimeException()
    val skillType = actualSkill.let { SkillType.find(actualSkill) } ?: throw RuntimeException()

    val nextVal = if (downgrade) {
      skillType.getPrevValue(
        actualSkill,
        min.ifBlank { null },
        max.ifBlank { null },
      )
    } else {
      skillType.getNextValue(actualSkill, min.ifBlank { null }, max.ifBlank { null })
    }

    if (Player.set(player, actualSkill, nextVal)) {
      maybeNotify(player, notify, nextVal)
    }
  }

  companion object {
    protected const val NO_VALUE = ""
  }
}
