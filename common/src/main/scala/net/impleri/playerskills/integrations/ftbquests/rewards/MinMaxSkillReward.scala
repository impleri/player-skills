package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.integrations.ftbquests.helpers.MinMaxTagHandling
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

abstract class MinMaxSkillReward[T](
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
)
  extends SkillReward[T](q, playerOps, skillOps, skillTypeOps) with MinMaxTagHandling[T] {
  override def writeData(nbt: CompoundTag): Unit = {
    super.writeData(nbt)
    writeMinMaxTags(nbt)
  }

  override def readData(nbt: CompoundTag): Unit = {
    super.readData(nbt)
    readMinMaxTags(nbt)
  }

  override def writeNetData(buffer: FriendlyByteBuf): Unit = {
    super.writeNetData(buffer)
    writeMinMaxBuffers(buffer)
  }

  override def readNetData(buffer: FriendlyByteBuf): Unit = {
    super.readNetData(buffer)
    readMinMaxBuffers(buffer)
  }

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit = {
    super.getConfig(config)
    addMinMaxToConfig(config)
  }

  override def getNextValue(player: Player[_]): Option[Skill[T]] = {
    val current = getPlayerValue(player)
    val skillType = getSkill.flatMap(skillTypeOps.get[T])
    val minMaxValue = skillType
      .flatMap(t => current.flatMap(s => if (downgrade) t.getPrevValue(s, min, max) else t.getNextValue(s, min, max)))
    val nextValue = value.orElse(minMaxValue)

    current.flatMap(c => playerOps.calculateValue(player, c, nextValue))
  }
}
