package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

import scala.util.chaining.scalaUtilChainingOps
import scala.util.Try

trait BooleanQuest extends QuestStateOps[Boolean] {
  data = QuestState(BasicSkillType.NAME)

  override protected val noneValue: Boolean = false

  override def writeValueToTag(
    nbt: NbtContents,
    key: String,
    value: Option[Boolean],
  ): NbtContents = {
    nbt.putBoolean(key, value.getOrElse(noneValue))
  }

  override def readValueFromTag(
    nbt: NbtContents,
    key: String,
  ): Option[Boolean] = nbt.getBoolean(key)

  override def writeValueToBuffer(
    buffer: FriendlyBuffer,
    value: Option[Boolean],
  ): FriendlyBuffer = {
    buffer.writeBoolean(value.getOrElse(noneValue))
  }

  override def readValueFromBuffer(buffer: FriendlyBuffer): Option[Boolean] =
    buffer.readBoolean()

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Boolean,
    options: NameMap[Boolean],
    defaultValue: Boolean,
  ): ConfigValue[_] = {
    config.addBool(
      key,
      value,
      v => data.copy(value = Try(v.booleanValue()).toOption).pipe(upsert),
      defaultValue,
    )
  }
}
