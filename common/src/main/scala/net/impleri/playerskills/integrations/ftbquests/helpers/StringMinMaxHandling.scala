package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait StringMinMaxHandling extends MinMaxTagHandling[String] {
  override def writeMinMaxTag(nbt: CompoundTag, key: String, value: Option[String]): Unit = {
    nbt.putString(key, value.getOrElse(noneValue))
  }

  override def readMinMaxTag(nbt: CompoundTag, key: String): Option[String] = {
    val rawValue = nbt.getString(key)
    if (rawValue.isBlank) None else Option(rawValue)
  }

  override def writeMinMaxBuffer(buffer: FriendlyByteBuf, value: Option[String]): Unit = {
    buffer.writeUtf(value.getOrElse(noneValue), Short.MaxValue)
  }

  override def readMinMaxBuffer(buffer: FriendlyByteBuf): Option[String] = {
    val rawValue = buffer.readUtf(Short.MaxValue)
    if (rawValue.isBlank) None else Option(rawValue)
  }

  @Environment(EnvType.CLIENT)
  override def addMinMaxToConfig(config: ConfigGroup): Unit = {
    val options: List[String] = getActualOptions

    if (options.nonEmpty) {
      config.addEnum(
          "min",
          min.getOrElse(options.head),
          (v: String) => min = if (v.isBlank) None else Option(v),
          getOptionsEnum(noneValue),
          noneValue,
        )
        .setNameKey("playerskills.quests.ui.min")
      config.addEnum(
          "max",
          max.getOrElse(options.last),
          (v: String) => max = if (v.isBlank) None else Option(v),
          getOptionsEnum(noneValue),
          noneValue,
        )
        .setNameKey("playerskills.quests.ui.max")
    }
  }
}
