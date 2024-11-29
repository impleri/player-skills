package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.quests.RestrictableValue
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.slab.entity.Player
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

import scala.util.chaining.scalaUtilChainingOps

abstract class RestrictableReward[T](
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends SkillReward[T](q, playerOps, skillOps, skillTypeOps)
    with RestrictableValue[T] {
  override def writeRewardDataTags(nbt: NbtContents): NbtContents =
      nbt
        .tap(super.writeRewardDataTags)
        .tap(writeMinMaxToTag)

  override def readRewardData(nbt: NbtContents): NbtContents =
    nbt
      .tap(super.readRewardData)
      .tap(readMinMaxFromTag)

  override def writeNetRewardData(buffer: FriendlyBuffer): Unit =
    if (data.isValid) {
      buffer
        .tap(super.writeNetRewardData)
        .tap(writeMinMaxToBuffer)
    }

  override def readNetRewardData(buffer: FriendlyBuffer): FriendlyBuffer =
    buffer
      .tap(super.readNetRewardData)
      .tap(readMinMaxFromBuffer)

  override def createConfig(config: ConfigGroup): ConfigGroup =
    config
      .tap(super.getConfig)
      .tap(addMinMaxToConfig)

  override def getNextValue(player: Player): Option[Skill[T]] =
    for {
      skillName <- data.skill
      skill <- playerOps.get[T](player, skillName)
      skillType <- skillTypeOps.get[T](skill)
      regradeValue = if (data.downgrade) skillType.getPrevValue(skill, data.min, data.max) else skillType.getNextValue(skill, data.min, data.max)
      desiredValue = data.value.filterNot(_ == noneValue).orElse(regradeValue)
      nextValue <- playerOps.calculateValue(player, skill, desiredValue)
    } yield nextValue
}
