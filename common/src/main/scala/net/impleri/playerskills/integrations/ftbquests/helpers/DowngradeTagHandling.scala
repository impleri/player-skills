package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component

trait DowngradeTagHandling[T] extends SkillValueHandling[T] {
  protected var downgrade = false

  protected def writeDowngradeTag(nbt: CompoundTag): Unit = {
    nbt.putBoolean("downgrade", downgrade)
  }

  protected def readDowngradeTag(nbt: CompoundTag): Unit = {
    downgrade = nbt.getBoolean("downgrade")
  }

  protected def writeDowngradeBuffer(buffer: FriendlyByteBuf): Unit = {
    buffer.writeBoolean(downgrade)
  }

  protected def readDowngradeBuffer(buffer: FriendlyByteBuf): Unit = {
    downgrade = buffer.readBoolean()
  }

  private def onChangeDowngrade(v: Boolean): Unit = {
    downgrade = v
    value = None
  }

  protected def addDowngradeToConfig(config: ConfigGroup): Unit = {
    config.addBool(
      "downgrade",
      downgrade,
      v => onChangeDowngrade(v),
      false,
    )
  }

  protected def maybeNotify(player: Player[_], notify: Boolean, value: Option[String] = None): Unit = {
    if (notify) {
      val messageSuffix = if (value.nonEmpty) "_value" else ""
      val messageBase = if (downgrade) "playerskills.quests.reward.downgrade" else "playerskills.quests.reward.upgrade"
      val messageKey = s"$messageBase$messageSuffix"


      player.sendMessage(Component.translatable(messageKey, getSkill, value))
    }
  }
}
