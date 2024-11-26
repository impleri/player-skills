package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.Reward
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.quests.Downgradable
import net.impleri.playerskills.integrations.ftbquests.quests.QuestStateOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

import scala.util.chaining.scalaUtilChainingOps

abstract class SkillReward[T](
  q: Quest,
  protected val playerOps: PlayerOps,
  protected val skillOps: SkillOps,
  protected val skillTypeOps: SkillTypeOps,
  protected val logger: Logger = PlayerSkillsLogger.FTB,
) extends Reward(q)
    with QuestStateOps[T]
    with Downgradable[T] {
  protected def writeRewardDataTags(nbt: NbtContents): NbtContents =
      nbt
        .pipe(writeSkillTag)
        .pipe(writeValueTag)
        .pipe(writeDowngradeTag)

  private def writeRewardData(nbt: NbtContents): Unit =
    if (data.isValid) {
      writeRewardDataTags(nbt).commit()
    }

  override def writeData(nbt: CompoundTag): Unit =
      nbt
        .tap(super.writeData)
        .pipe(NbtContents(_))
        .pipe(writeRewardData)

  protected def readRewardData(nbt: NbtContents): NbtContents =
    nbt
      .tap(readSkillTag)
      .tap(readValueTag)
      .tap(readDowngradeTag)

  override def readData(nbt: CompoundTag): Unit =
    nbt
      .tap(super.readData)
      .pipe(NbtContents(_))
      .tap(readRewardData)

  protected def writeNetRewardData(buffer: FriendlyBuffer): Unit =
        buffer
          .pipe(writeSkillBuffer)
          .pipe(writeValueBuffer)
          .pipe(writeDowngradeBuffer)

  override def writeNetData(buffer: FriendlyByteBuf): Unit =
      buffer
        .tap(super.writeNetData)
        .pipe(FriendlyBuffer(_))
        .pipe(writeNetRewardData)

  protected def readNetRewardData(buffer: FriendlyBuffer): FriendlyBuffer =
    buffer
      .tap(readSkillBuffer)
      .tap(readValueBuffer)
      .tap(readDowngradeBuffer)

  override def readNetData(buffer: FriendlyByteBuf): Unit =
    buffer
      .tap(super.readNetData)
      .pipe(FriendlyBuffer(_))
      .tap(readNetRewardData)

  protected def createConfig(config: ConfigGroup): ConfigGroup =
    config
      .tap(addSkillToConfig)
      .tap(addValueToConfig)
      .tap(addDowngradeToConfig)

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit =
    config
      .tap(super.getConfig)
      .tap(createConfig)

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent =
    getSkillTitle.mutableOutput

  override def ignoreRewardBlocking(): Boolean =
    true

  override def isIgnoreRewardBlockingHardcoded: Boolean =
    true

  protected def getNextValue(player: Player): Option[Skill[T]] =
    for {
      skillName <- data.skill
      skill <- playerOps.get[T](player, skillName)
      skillType <- skillTypeOps.get[T](skill)
      regradeValue = if (data.downgrade) skillType.getPrevValue(skill) else skillType.getNextValue(skill)
      desiredValue = data.value.orElse(regradeValue)
      nextValue <- playerOps.calculateValue(player, skill, desiredValue)
    } yield nextValue

  override def claim(p: ServerPlayer, n: Boolean): Unit =
    for {
      player <- Option(p).map(Player(_))
      notify = Option(n).getOrElse(false)
      nextValue <- getNextValue(player)
      _ = logger.debug(s"Changing ${nextValue.name} to ${nextValue.value} for $player")
      updatedSkills = playerOps.upsert(player, nextValue)
      updated <- updatedSkills.find(s => s.name == nextValue.name && s.value == nextValue.value)
    } yield maybeNotify(player, notify, updated.value.toString)
}
