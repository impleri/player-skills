package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait SkillValueHandling[T] extends SkillTagHandling {
  protected def playerOps: PlayerOps

  protected var value: Option[T] = None

  protected def noneValue: T

  protected def writeValueToTag(nbt: CompoundTag, key: String, value: Option[T]): Unit = ???

  protected def writeValueTag(nbt: CompoundTag): Unit = {
    writeValueToTag(nbt, "value", value)
  }

  protected def readValueFromTag(nbt: CompoundTag, key: String): Option[T] = ???

  protected def readValueTag(nbt: CompoundTag): Unit = {
    value = readValueFromTag(nbt, "value")
  }

  protected def writeValueToBuffer(buffer: FriendlyByteBuf, value: Option[T]): Unit = ???

  protected def writeValueBuffer(buffer: FriendlyByteBuf): Unit = {
    writeValueToBuffer(buffer, value)
  }

  protected def readValueFromBuffer(buffer: FriendlyByteBuf): Option[T] = ???

  protected def readValueBuffer(buffer: FriendlyByteBuf): Unit = {
    value = readValueFromBuffer(buffer)
  }

  protected def handleRawValue(rawValue: T): Option[T] = {
    if (rawValue == noneValue) None else Option(rawValue)
  }

  protected def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Option[T],
    options: NameMap[T],
    defaultValue: T,
  ): ConfigValue[_] = {
    ???
  }

  @Environment(EnvType.CLIENT)
  protected def addValueToConfig(config: ConfigGroup): Unit = {
    val actualSkill = getActualSkill[T]
    val options = getActualOptions

    if (!actualSkill.exists(_.isAllowedValue(value))) {
      value = options.headOption
    }

    val valueConfig: ConfigValue[_] = if (options.isEmpty) {
      addValueConfig(
        config,
        "value",
        value,
        getOptionsEnum(noneValue),
        noneValue,
      )
    } else {
      config.addEnum(
        "value",
        value.getOrElse(options.head),
        (v: T) => value = handleRawValue(v),
        getOptionsEnum(noneValue),
        noneValue,
      )
    }

    valueConfig.setNameKey("playerskills.quests.ui.value")
  }

  protected def isCompleted(player: Player[_], expected: Option[T] = None): Boolean = {
    getSkill.exists(s => playerOps.can(player.uuid, s, expected))
  }
}
