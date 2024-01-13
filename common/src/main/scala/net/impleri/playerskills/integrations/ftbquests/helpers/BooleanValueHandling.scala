package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait BooleanValueHandling extends SkillValueHandling[Boolean] {
  override val noneValue: Boolean = false

  override def writeValueToTag(nbt: CompoundTag, key: String, value: Option[Boolean]): Unit = {
    nbt.putBoolean(key, value.getOrElse(false))
  }

  override def readValueFromTag(nbt: CompoundTag, key: String): Option[Boolean] = handleRawValue(nbt.getBoolean(key))

  override def writeValueToBuffer(buffer: FriendlyByteBuf, value: Option[Boolean]): Unit = {
    buffer.writeBoolean(value.getOrElse(false))
  }

  override def readValueFromBuffer(buffer: FriendlyByteBuf): Option[Boolean] = handleRawValue(buffer.readBoolean())

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Option[Boolean],
    options: NameMap[Boolean],
    defaultValue: Boolean,
  ): ConfigValue[_] = {
    config.addBool(key, value.getOrElse(defaultValue), v => handleRawValue(v), defaultValue)
  }
}
