package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.Task
import dev.ftb.mods.ftbquests.quest.TeamData
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.integrations.ftbquests.quests.QuestStateOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item
import net.impleri.slab.logging.Logger
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
  override val logger: Logger = PlayerSkillsLogger.FTB,
) extends Task(q)
    with QuestStateOps[T] {

  private def maxProgress: Option[Long] = data.value match {
    case Some(numeric: Double) => Option(numeric.toLong min SkillTask.MINIMUM_VALUE)
    case Some(_)               => Option(SkillTask.MINIMUM_VALUE)
    case _                     => None
  }

  override def formatMaxProgress(): String = maxProgress.fold(SkillTask.MINIMUM_PROGRESS)(_.toString)

  private def getProgress(progress: Long): Long = maxProgress
    .filter(progress < _)
    .fold(progress)(identity)

  override def formatProgress(teamData: TeamData, progress: Long): String =
    Option(progress)
      .map(getProgress)
      .fold(SkillTask.MINIMUM_PROGRESS)(_.toString)

  private def writeTaskDataTags(nbt: NbtContents): NbtContents =
    nbt
      .pipe(writeSkillTag)
      .pipe(writeValueTag)

  private def writeTaskData(nbt: NbtContents): Unit =
    if (data.isValid) {
      writeTaskDataTags(nbt).commit()
    }

  override def writeData(nbt: CompoundTag): Unit =
    nbt
      .tap(super.writeData)
      .pipe(NbtContents(_))
      .pipe(writeTaskData)

  private def readTaskData(nbt: NbtContents): NbtContents =
    nbt
      .tap(readSkillTag)
      .tap(readValueTag)

  override def readData(nbt: CompoundTag): Unit =
    nbt
      .tap(super.readData)
      .pipe(NbtContents(_))
      .tap(readTaskData)

  private def writeNetTaskData(buffer: FriendlyBuffer): Unit =
    buffer
      .pipe(writeSkillBuffer)
      .pipe(writeValueBuffer)

  override def writeNetData(buffer: FriendlyByteBuf): Unit =
    buffer
      .tap(super.writeNetData)
      .pipe(FriendlyBuffer(_))
      .pipe(writeNetTaskData)

  private def readNetTaskData(buffer: FriendlyBuffer): FriendlyBuffer =
    buffer
      .tap(readSkillBuffer)
      .tap(readValueBuffer)

  override def readNetData(buffer: FriendlyByteBuf): Unit =
    buffer
      .tap(super.readNetData)
      .pipe(FriendlyBuffer(_))
      .tap(readNetTaskData)

  protected def createConfig(config: ConfigGroup): ConfigGroup =
    config
      .tap(addSkillToConfig)
      .tap(addValueToConfig)

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit =
    config
      .tap(super.getConfig)
      .tap(createConfig)

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent = getSkillTitle.mutableOutput

  override def autoSubmitOnPlayerTick(): Int = SkillTask.CHECK_INTERVAL_TICKS

  override def submitTask(
    teamData: TeamData,
    serverPlayer: ServerPlayer,
    itemStack: Item.VanillaStack,
  ): Unit = {
    for {
      player <- Player.fromVanilla(serverPlayer)
      skill <- data.skill
    } yield {
      if (playerOps.can(player.uuid, skill, data.value)) {
        logger.debug(s"Completing $title for ${player.handle}")
        teamData.setProgress(this, maxProgress.getOrElse(1))
      }
    }
  }
}

object SkillTask {
  private final val CHECK_INTERVAL_TICKS: Int = 200 // 5 seconds should be more than often enough

  private final val MINIMUM_VALUE: Long = 1L

  private final val MINIMUM_PROGRESS: String = "0"
}
