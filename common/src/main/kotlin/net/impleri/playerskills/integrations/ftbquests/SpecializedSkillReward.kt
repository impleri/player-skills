package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.skills.specialized.SpecializedSkillType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class SpecializedSkillReward(quest: Quest?) : BasicSkillReward(quest) {
  var value: String = NO_VALUE
  override val skillType: ResourceLocation
    get() = SpecializedSkillType.NAME

  override fun getType(): RewardType {
    return SkillRewardTypes.SPECIALIZED_SKILL
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

    config.addEnum(
      "value", value, { value = it }, NameMap.of(
        options[0], options
      ).create(), actualSkill.value
    )
      .setNameKey("playerskills.quests.ui.value")
  }

  override fun claim(player: ServerPlayer, notify: Boolean) {
    val actualSkill = skill?.let { Player.get<String>(player, it) } ?: throw RuntimeException()

    if (Player.set(player, actualSkill, value)) {
      maybeNotify(player, notify, value)
    }
  }

  companion object {
    protected const val NO_VALUE = ""
  }
}
