package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.BooleanTask
import dev.ftb.mods.ftbquests.quest.TeamData
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.integrations.ftbquests.helpers.QuestStateOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.slab.entity.Player
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

import scala.util.chaining.scalaUtilChainingOps

abstract class SkillTask[T](
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
) extends BooleanTask(q)
    with QuestStateOps[T] {
  protected def writeData(nbt: NbtContents): NbtContents = {
    nbt
      .pipe(writeSkillTag)
      .pipe(writeValueTag)
  }

  override def writeData(nbt: CompoundTag): Unit = {
    super.writeData(nbt)
    writeData(NbtContents(nbt))
  }

  protected def readData(nbt: NbtContents): NbtContents = {
    nbt
      .tap(readSkillTag)
      .tap(readValueTag)
  }

  override def readData(nbt: CompoundTag): Unit = {
    super.readData(nbt)
    readData(NbtContents(nbt))
  }

  protected def writeNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .pipe(writeSkillBuffer)
      .pipe(writeValueBuffer)
  }

  override def writeNetData(buffer: FriendlyByteBuf): Unit = {
    super.writeNetData(buffer)
    writeNetData(FriendlyBuffer(buffer))
  }

  protected def readNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .tap(readSkillBuffer)
      .tap(readValueBuffer)
  }

  override def readNetData(buffer: FriendlyByteBuf): Unit = {
    super.readNetData(buffer)
    readNetData(FriendlyBuffer(buffer))
  }

  protected def createConfig(config: ConfigGroup): ConfigGroup = {
    config
      .tap(super.getConfig)
      .tap(addSkillToConfig)
      .tap(addValueToConfig)
  }

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit = {
    createConfig(config)
  }

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent = {
    getSkillTitle.mutableOutput
  }

  override def autoSubmitOnPlayerTick(): Int = 20

  private def isCompleted(
    player: Player[_],
    expected: Option[T] = None,
  ): Boolean = {
    data.skill.exists(s => playerOps.can(player.uuid, s, expected))
  }

  override def canSubmit(teamData: TeamData, player: ServerPlayer): Boolean = {
    isCompleted(Player(player))
  }
}
