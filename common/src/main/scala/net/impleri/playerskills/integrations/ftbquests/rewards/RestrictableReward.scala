package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftbquests.quest.Quest
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.helpers.RestrictableValue
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
)
  extends SkillReward[T](q, playerOps, skillOps, skillTypeOps) with RestrictableValue[T] {
  override def writeData(nbt: NbtContents): NbtContents = {
    nbt
      .pipe(super.writeData)
      .pipe(writeMinMaxToTag)
  }

  override def readData(nbt: NbtContents): NbtContents = {
    nbt
      .tap(super.readData)
      .tap(readMinMaxFromTag)
  }

  override def writeNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .pipe(super.writeNetData)
      .pipe(writeMinMaxToBuffer)
  }

  override def readNetData(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer
      .tap(super.readNetData)
      .tap(readMinMaxFromBuffer)
  }

  override def createConfig(config: ConfigGroup): ConfigGroup = {
    config
      .tap(super.getConfig)
      .tap(addMinMaxToConfig)
  }

  override def getNextValue(player: Player[_]): Option[Skill[T]] = {
    val current = getPlayerValue(player)
    val skillType = data.skill.flatMap(skillTypeOps.get[T])
    val minMaxValue = skillType
      .flatMap(t => current
        .flatMap(s => if (data.downgrade) t.getPrevValue(s, data.min, data.max) else t
          .getNextValue(s, data.min, data.max),
        ),
      )
    val nextValue = data.value.orElse(minMaxValue)

    current.flatMap(c => playerOps.calculateValue(player, c, nextValue))
  }
}
