package net.impleri.playerskills.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait StringQuest extends RestrictableValue[String] {
  override val noneValue = ""
  override val maxValue = ""

  override def writeValueToTag(
    nbt: NbtContents,
    key: String,
    value: Option[String],
  ): NbtContents = {
    nbt.putString(key, value.getOrElse(noneValue))
  }

  override def readValueFromTag(nbt: NbtContents, key: String): Option[String] =
    nbt.getString(key)

  override def writeMinMaxTag(
    nbt: NbtContents,
    key: String,
    value: Option[String],
  ): NbtContents = {
    nbt.putString(key, value.getOrElse(noneValue))
  }

  override def readMinMaxTag(nbt: NbtContents, key: String): Option[String] = {
    nbt.getString(key).filterNot(_.isBlank)
  }

  override def writeValueToBuffer(
    buffer: FriendlyBuffer,
    value: Option[String],
  ): FriendlyBuffer = {
    buffer.writeString(value.getOrElse(noneValue))
  }

  override def readValueFromBuffer(buffer: FriendlyBuffer): Option[String] = {
    buffer.readString()
  }

  override def writeMinMaxBuffer(
    buffer: FriendlyBuffer,
    value: Option[String],
  ): FriendlyBuffer = {
    buffer.writeString(value.getOrElse(noneValue))
  }

  override def readMinMaxBuffer(buffer: FriendlyBuffer): Option[String] = {
    buffer.readString().filterNot(_.isBlank)
  }

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: String,
    options: NameMap[String],
    defaultValue: String,
  ): ConfigValue[_] = {
    config
      .addString(
        key,
        value,
        (v: String) =>
          Option(v)
            .filterNot(_.isBlank)
            .pipe(n => data.copy(value = n))
            .pipe(upsert),
        defaultValue,
      )
  }

  override protected def addMinMaxConfig(
    config: ConfigGroup,
    name: String,
    value: String,
    f: Option[String] => Unit,
  ): Option[ConfigValue[_]] = {
    val options = data.skill
      .flatMap(skillOps.get)
      .map(_.options)

    options
      .filter(_.nonEmpty)
      .map(List(noneValue) ++ _)
      .map(o =>
        NameMap
          .of(
            options.get.headOption.getOrElse(noneValue),
            o.asJava,
          )
          .create(),
      )
      .map(options =>
        config.addEnum(
          name,
          value,
          (v: String) => Option(v).filterNot(_.isBlank).pipe(f),
          options,
          noneValue,
        ),
      )
  }
}
