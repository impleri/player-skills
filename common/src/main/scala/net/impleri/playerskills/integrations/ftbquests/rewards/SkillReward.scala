package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.Reward
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.integrations.ftbquests.helpers.DowngradeTagHandling
import net.impleri.playerskills.integrations.ftbquests.helpers.SkillValueHandling
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

abstract class SkillReward[T](
  q: Quest,
  protected val playerOps: PlayerOps,
  protected val skillOps: SkillOps,
  protected val skillTypeOps: SkillTypeOps,
) extends Reward(q) with SkillValueHandling[T] with DowngradeTagHandling[T] {
  autoclaim = RewardAutoClaim.INVISIBLE

  override def writeData(nbt: CompoundTag): Unit = {
    super.writeData(nbt)
    writeSkillTag(nbt)
    writeValueTag(nbt)
    writeDowngradeTag(nbt)
  }

  override def readData(nbt: CompoundTag): Unit = {
    super.readData(nbt)
    readSkillTag(nbt)
    readValueTag(nbt)
    readDowngradeTag(nbt)
  }

  override def writeNetData(buffer: FriendlyByteBuf): Unit = {
    super.writeNetData(buffer)
    writeSkillBuffer(buffer)
    writeValueBuffer(buffer)
    writeDowngradeBuffer(buffer)
  }

  override def readNetData(buffer: FriendlyByteBuf): Unit = {
    super.readNetData(buffer)
    readSkillBuffer(buffer)
    readValueBuffer(buffer)
    readDowngradeBuffer(buffer)
  }

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit = {
    super.getConfig(config)
    addSkillToConfig(config)
    addValueToConfig(config)
    addDowngradeToConfig(config)
  }

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent = {
    getSkillTitle
  }

  override def ignoreRewardBlocking(): Boolean = {
    true
  }

  override def isIgnoreRewardBlockingHardcoded: Boolean = {
    true
  }

  protected def getPlayerValue(player: Player[_]): Option[Skill[T]] = getSkill.flatMap(s => playerOps.get[T](player, s))

  protected def getNextValue(player: Player[_]): Option[Skill[T]] = {
    val current = getSkill.flatMap(s => playerOps.get[T](player, s))
    val skillType = getSkill.flatMap(skillTypeOps.get[T])
    val regradeValue = skillType
      .flatMap(t => current.flatMap(s => if (downgrade) t.getPrevValue(s) else t.getNextValue(s)))
    val nextValue = value.orElse(regradeValue)

    current.flatMap(c => playerOps.calculateValue(player, c, nextValue))
  }

  override def claim(p: ServerPlayer, notify: Boolean): Unit = {
    val player = Player(p)
    val newVal = getNextValue(player)
      .flatMap(v => playerOps.upsert(player, v).find(s => s.name == v.name && s.value == v.value))
      .flatMap(_.value.asInstanceOf[Option[T]])

    if (newVal.nonEmpty) {
      maybeNotify(player, notify, newVal.map(_.toString))
    }
  }
}
