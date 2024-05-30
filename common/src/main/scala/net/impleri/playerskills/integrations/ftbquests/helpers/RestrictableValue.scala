package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

import scala.util.chaining.scalaUtilChainingOps

trait RestrictableValue[T] extends QuestStateOps[T] {
  protected def maxValue: T = noneValue

  protected def writeMinMaxTag(nbt: NbtContents, key: String, value: Option[T]): NbtContents

  protected def writeMinMaxToTag(nbt: NbtContents): NbtContents = {
    writeMinMaxTag(nbt, RestrictableValue.MIN_TAG_NAME, data.min)
    writeMinMaxTag(nbt, RestrictableValue.MAX_TAG_NAME, data.max)
  }

  protected def readMinMaxTag(nbt: NbtContents, key: String): Option[T]

  protected def readMinMaxFromTag(nbt: NbtContents): Unit = {
    val min = readMinMaxTag(nbt, RestrictableValue.MIN_TAG_NAME)
    val max = readMinMaxTag(nbt, RestrictableValue.MAX_TAG_NAME)

    upsert(data.copy(min = min, max = max))
  }

  protected def writeMinMaxBuffer(buffer: FriendlyBuffer, value: Option[T]): FriendlyBuffer

  protected def writeMinMaxToBuffer(buffer: FriendlyBuffer): FriendlyBuffer = {
    writeMinMaxBuffer(buffer, data.min)
    writeMinMaxBuffer(buffer, data.max)
  }

  protected def readMinMaxBuffer(buffer: FriendlyBuffer): Option[T]

  protected def readMinMaxFromBuffer(buffer: FriendlyBuffer): Unit = {
    val min = readMinMaxBuffer(buffer)
    val max = readMinMaxBuffer(buffer)

    upsert(data.copy(min = min, max = max))
  }

  protected def addMinMaxConfig(
    config: ConfigGroup,
    name: String,
    value: T,
    f: Option[T] => Unit,
  ): Option[ConfigValue[_]]

  protected def addMinMaxToConfig(config: ConfigGroup): Unit = {
    addMinMaxConfig(
      config,
      RestrictableValue.MIN_TAG_NAME,
      data.min.getOrElse(noneValue),
      v => data.copy[T](min = v).pipe(upsert),
    ).foreach(_.setNameKey(QuestStateOps.uiKey(RestrictableValue.MIN_TAG_NAME)))

    addMinMaxConfig(
      config,
      RestrictableValue.MAX_TAG_NAME,
      data.max.getOrElse(noneValue),
      v => data.copy[T](max = v).pipe(upsert),
    ).foreach(_.setNameKey(QuestStateOps.uiKey(RestrictableValue.MAX_TAG_NAME)))
  }
}

object RestrictableValue {
  private final val MAX_TAG_NAME = "max"

  private final val MIN_TAG_NAME = "min"
}
