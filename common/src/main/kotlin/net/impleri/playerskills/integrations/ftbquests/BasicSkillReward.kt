package net.impleri.playerskills.integrations.ftbquests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.Reward
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim
import dev.ftb.mods.ftbquests.quest.reward.RewardType
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

open class BasicSkillReward(quest: Quest?) : Reward(quest) {
  var skill: ResourceLocation? = null
  var downgrade = false

  init {
    autoclaim = RewardAutoClaim.INVISIBLE
  }

  protected open val skillType: ResourceLocation
    get() = BasicSkillType.NAME

  override fun getType(): RewardType {
    return SkillRewardTypes.BASIC_SKILL
  }

  override fun writeData(nbt: CompoundTag) {
    super.writeData(nbt)

    nbt.putString("skill", skill.toString())

    if (downgrade) {
      nbt.putBoolean("downgrade", true)
    }
  }

  override fun readData(nbt: CompoundTag) {
    super.readData(nbt)
    skill = SkillResourceLocation.of(nbt.getString("skill"))
    downgrade = nbt.getBoolean("downgrade")
  }

  override fun writeNetData(buffer: FriendlyByteBuf) {
    super.writeNetData(buffer)
    buffer.writeUtf(skill.toString(), Short.MAX_VALUE.toInt())
    buffer.writeBoolean(downgrade)
  }

  override fun readNetData(buffer: FriendlyByteBuf) {
    super.readNetData(buffer)
    skill = SkillResourceLocation.of(buffer.readUtf(Short.MAX_VALUE.toInt()))
    downgrade = buffer.readBoolean()
  }

  @Environment(EnvType.CLIENT)
  override fun getConfig(config: ConfigGroup) {
    super.getConfig(config)
    val skills = PlayerSkillsIntegration.getSkills(
      skillType,
    )

    val firstSkill = skills.firstOrNull()
    skill = skill ?: firstSkill

    config.addEnum(
      "skill",
      skill,
      { skill = it },
      NameMap.of(firstSkill, skills).create(),
      firstSkill,
    ).setNameKey("playerskills.quests.ui.skill")
    config.addBool("downgrade", downgrade, { downgrade = it }, false)
      .setNameKey("playerskills.quests.ui.downgrade")
  }

  @Environment(EnvType.CLIENT)
  override fun getAltTitle(): MutableComponent {
    return Component.translatable("playerskills.quests.ui.skill").append(": ").append(
      Component.literal(skill.toString()).withStyle(ChatFormatting.YELLOW),
    )
  }

  override fun ignoreRewardBlocking(): Boolean {
    return true
  }

  override fun isIgnoreRewardBlockingHardcoded(): Boolean {
    return true
  }

  protected fun maybeNotify(player: ServerPlayer, notify: Boolean, value: String? = null) {
    if (!notify) {
      return
    }

    var messageKey = if (downgrade) "playerskills.quests.reward.downgrade" else "playerskills.quests.reward.upgrade"
    value?.let { messageKey += "_value" }

    player.sendSystemMessage(Component.translatable(messageKey, skill, value), true)
  }

  override fun claim(player: ServerPlayer, notify: Boolean) {
    val actualSkill = skill?.let { Player.get<Boolean>(player, it) } ?: throw RuntimeException()
    if (Player.set(player, actualSkill, !downgrade)) {
      maybeNotify(player, notify)
    }
  }
}
