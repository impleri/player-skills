package net.impleri.playerskills.server.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait StringValueHandling extends SkillValueHandling[String] with StringMinMaxHandling {
  override val noneValue = ""

  override def writeValueToTag(nbt: CompoundTag, key: String, value: Option[String]): Unit = {
    nbt.putString(key, value.getOrElse(noneValue))
  }

  override def readValueFromTag(nbt: CompoundTag, key: String): Option[String] = handleRawValue(nbt.getString(key))

  override def writeValueToBuffer(buffer: FriendlyByteBuf, value: Option[String]): Unit = {
    buffer.writeUtf(value.getOrElse(noneValue), Short.MaxValue)
  }

  override def readValueFromBuffer(buffer: FriendlyByteBuf): Option[String] = {
    handleRawValue(buffer
      .readUtf(Short.MaxValue),
    )
  }

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Option[String],
    options: NameMap[String],
    defaultValue: String,
  ): ConfigValue[_] = {
    config
      .addString(key,
        value.getOrElse(defaultValue),
        v => handleRawValue(v),
        defaultValue,
      )
  }
}
