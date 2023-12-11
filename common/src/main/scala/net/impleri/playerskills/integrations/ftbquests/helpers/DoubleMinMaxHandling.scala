package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait DoubleMinMaxHandling extends MinMaxTagHandling[Double] {
  override def writeMinMaxTag(nbt: CompoundTag, key: String, value: Option[Double]): Unit = {
    nbt.putDouble(key, value.getOrElse(noneValue))
  }

  override def readMinMaxTag(nbt: CompoundTag, key: String): Option[Double] = {
    handleRawValue(nbt.getDouble(key))
  }

  override def writeMinMaxBuffer(buffer: FriendlyByteBuf, value: Option[Double]): Unit = {
    buffer.writeDouble(value.getOrElse(noneValue))
  }

  override def readMinMaxBuffer(buffer: FriendlyByteBuf): Option[Double] = {
    handleRawValue(buffer.readDouble())
  }

  @Environment(EnvType.CLIENT)
  override def addMinMaxToConfig(config: ConfigGroup): Unit = {
    config
      .addDouble("min",
        min.getOrElse(noneValue),
        v => min = handleRawValue(v),
        noneValue,
        noneValue,
        Double.MaxValue,
      )
      .setNameKey("playerskills.quests.ui.min")
    config
      .addDouble("max",
        max.getOrElse(Double.MaxValue),
        v => max = if (v == Double.MaxValue) None else handleRawValue(v),
        noneValue,
        noneValue,
        Double.MaxValue,
      )
      .setNameKey("playerskills.quests.ui.max")
  }
}
