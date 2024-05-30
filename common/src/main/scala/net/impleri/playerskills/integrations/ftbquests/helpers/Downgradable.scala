package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.entity.Player
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

trait Downgradable[T] extends QuestStateOps[T] {
  protected def writeDowngradeTag(nbt: NbtContents): NbtContents = {
    nbt.putBoolean(Downgradable.DOWNGRADE_TAG_NAME, data.downgrade)
  }

  protected def readDowngradeTag(nbt: NbtContents): Unit = {
    upsert(data
      .copy(downgrade = nbt
        .getBoolean(Downgradable.DOWNGRADE_TAG_NAME)
        .getOrElse(Downgradable.DEFAULT_VALUE),
      ),
    )
  }

  protected def writeDowngradeBuffer(buffer: FriendlyBuffer): FriendlyBuffer = {
    buffer.writeBoolean(data.downgrade)
  }

  protected def readDowngradeBuffer(buffer: FriendlyBuffer): Unit = {
    upsert(data.copy(downgrade = buffer.readBoolean().getOrElse(Downgradable.DEFAULT_VALUE)))
  }

  protected def addDowngradeToConfig(config: ConfigGroup): Unit = {
    config.addBool(
      Downgradable.DOWNGRADE_TAG_NAME,
      data.downgrade,
      v => Option(v)
        .filterNot(_ == data.downgrade)
        .map(d => data.copy[T](downgrade = d, value = None))
        .foreach(upsert),
      false,
    )
  }

  protected def maybeNotify(player: Player.Any, notify: Boolean, value: String): Unit = {
    Option(Downgradable.DOWNGRADE_TAG_NAME)
      .filter(_ => data.downgrade)
      .orElse(Option(Downgradable.UPGRADE_TAG_NAME))
      .map(QuestStateOps.rewardKey)
      .map(m => if (value.nonEmpty) s"${m}_value" else m)
      .filter(_ => notify)
      .foreach(m => player.sendMessage(TranslatableText(m, data.skillAsString, data.value)))
  }
}

object Downgradable {
  private final val DEFAULT_VALUE = false

  private final val DOWNGRADE_TAG_NAME = "downgrade"

  private final val UPGRADE_TAG_NAME = "upgrade"
}
