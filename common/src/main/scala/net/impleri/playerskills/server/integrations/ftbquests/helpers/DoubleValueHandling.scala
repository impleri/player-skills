package net.impleri.playerskills.server.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait DoubleValueHandling extends SkillValueHandling[Double] with DoubleMinMaxHandling {
  override val noneValue = 0.0

  override def writeValueToTag(nbt: CompoundTag, key: String, value: Option[Double]): Unit = {
    nbt.putDouble(key, value.getOrElse(noneValue))
  }

  override def readValueFromTag(nbt: CompoundTag, key: String): Option[Double] = handleRawValue(nbt.getDouble(key))

  override def writeValueToBuffer(buffer: FriendlyByteBuf, value: Option[Double]): Unit = {
    buffer.writeDouble(value.getOrElse(noneValue))
  }

  override def readValueFromBuffer(buffer: FriendlyByteBuf): Option[Double] = handleRawValue(buffer.readDouble())

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Option[Double],
    options: NameMap[Double],
    defaultValue: Double,
  ): ConfigValue[_] = {
    config
      .addDouble(key,
        value.getOrElse(defaultValue),
        v => handleRawValue(v),
        defaultValue,
        min.getOrElse(noneValue),
        max.getOrElse(Double.MaxValue),
      )
  }
}
