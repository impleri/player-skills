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
import net.impleri.playerskills.integrations.ftbquests.helpers.Downgradable
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

abstract class SkillReward[T](
  q: Quest,
  protected val playerOps: PlayerOps,
  protected val skillOps: SkillOps,
  protected val skillTypeOps: SkillTypeOps,
) extends Reward(q) with QuestStateOps[T] with Downgradable[T] {
  autoclaim = RewardAutoClaim.INVISIBLE

  protected def writeData(nbt: NbtContents): NbtContents = {
    nbt
      .pipe(writeSkillTag)
      .pipe(writeValueTag)
      .pipe(writeDowngradeTag)
  }

  override def writeData(nbt: CompoundTag): Unit = {
    super.writeData(nbt)
    writeData(NbtContents(nbt))
  }

  protected def readData(nbt: NbtContents): NbtContents = {
    nbt
      .tap(readSkillTag)
      .tap(readValueTag)
      .tap(readDowngradeTag)
  }

  override def readData(nbt: CompoundTag): Unit = {
    super.readData(nbt)
    readData(NbtContents(nbt))
  }

  protected def writeNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .pipe(writeSkillBuffer)
      .pipe(writeValueBuffer)
      .pipe(writeDowngradeBuffer)
  }

  override def writeNetData(buffer: FriendlyByteBuf): Unit = {
    super.writeNetData(buffer)
    writeNetData(FriendlyBuffer(buffer))
  }

  protected def readNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .tap(readSkillBuffer)
      .tap(readValueBuffer)
      .tap(readDowngradeBuffer)
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
      .tap(addDowngradeToConfig)
  }

  @Environment(EnvType.CLIENT)
  override def getConfig(config: ConfigGroup): Unit = {
    createConfig(config)
  }

  @Environment(EnvType.CLIENT)
  override def getAltTitle: MutableComponent = {
    getSkillTitle.mutableOutput
  }

  override def ignoreRewardBlocking(): Boolean = {
    true
  }

  override def isIgnoreRewardBlockingHardcoded: Boolean = {
    true
  }

  protected def getPlayerValue(player: Player[_]): Option[Skill[T]] = data
    .skill
    .flatMap(s => playerOps.get[T](player, s))

  protected def getNextValue(player: Player[_]): Option[Skill[T]] = {
    val current = data.skill.flatMap(s => playerOps.get[T](player, s))
    val skillType = data.skill.flatMap(skillTypeOps.get[T])
    val regradeValue = skillType
      .flatMap(t => current.flatMap(s => if (data.downgrade) t.getPrevValue(s) else t.getNextValue(s)))
    val nextValue = data.value.orElse(regradeValue)

    current.flatMap(c => playerOps.calculateValue(player, c, nextValue))
  }

  override def claim(p: ServerPlayer, n: Boolean): Unit = {
    for {
      player <- Option(p).map(Player(_))
      notify = Option(n).getOrElse(false)
      _ = getNextValue(player)
        .flatMap(v => playerOps.upsert(player, v).find(s => s.name == v.name && s.value == v.value))
        .flatMap(_.value.asInstanceOf[Option[T]])
        .foreach(v => maybeNotify(player, notify, v.toString))
    } yield {}
  }
}
