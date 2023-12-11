package net.impleri.playerskills.server.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.BooleanTask
import dev.ftb.mods.ftbquests.quest.TeamData
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.integrations.ftbquests.helpers.SkillValueHandling
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

abstract class SkillTask[T](
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
) extends BooleanTask(q) with SkillValueHandling[T] {
  override def writeData(nbt: CompoundTag): Unit = {
    super.writeData(nbt)
    writeSkillTag(nbt)
    writeValueTag(nbt)
  }

  override def readData(nbt: CompoundTag): Unit = {
    super.readData(nbt)
    readSkillTag(nbt)
    readValueTag(nbt)
  }

  override def writeNetData(buffer: FriendlyByteBuf): Unit = {
    super.writeNetData(buffer)
    writeSkillBuffer(buffer)
    writeValueBuffer(buffer)
  }

  override def readNetData(buffer: FriendlyByteBuf): Unit = {
    super.readNetData(buffer)
    readSkillBuffer(buffer)
    readValueBuffer(buffer)
  }

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit = {
    super.getConfig(config)
    addSkillToConfig(config)
    addValueToConfig(config)
  }

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent = {
    getSkillTitle
  }

  override def autoSubmitOnPlayerTick(): Int = 20

  override def canSubmit(teamData: TeamData, player: ServerPlayer): Boolean = {
    isCompleted(Player(player))
  }
}
