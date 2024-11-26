package net.impleri.playerskills.integrations.ftbquests.quests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer

import scala.util.chaining.scalaUtilChainingOps

trait DoubleQuest extends RestrictableValue[Double] {
  data = QuestState(NumericSkillType.NAME)

  override val noneValue = 0.0

  override protected val maxValue: Double = Double.MaxValue

  override def writeValueToTag(
    nbt: NbtContents,
    key: String,
    value: Option[Double],
  ): NbtContents =
    nbt.putDouble(key, value.getOrElse(noneValue))

  override def readValueFromTag(nbt: NbtContents, key: String): Option[Double] =
    nbt.getDouble(key)

  override def writeMinMaxTag(
    nbt: NbtContents,
    key: String,
    value: Option[Double],
  ): NbtContents =
    nbt.putDouble(key, value.getOrElse(noneValue))

  override def readMinMaxTag(nbt: NbtContents, key: String): Option[Double] =
    nbt.getDouble(key)

  override def writeValueToBuffer(
    buffer: FriendlyBuffer,
    value: Option[Double],
  ): FriendlyBuffer =
    buffer.writeDouble(value.getOrElse(noneValue))

  override def readValueFromBuffer(buffer: FriendlyBuffer): Option[Double] =
    buffer.readDouble()

  override def writeMinMaxBuffer(
    buffer: FriendlyBuffer,
    value: Option[Double],
  ): FriendlyBuffer =
    buffer.writeDouble(value.getOrElse(noneValue))

  override def readMinMaxBuffer(buffer: FriendlyBuffer): Option[Double] =
    buffer.readDouble()

  override def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: Double,
    options: NameMap[Double],
    defaultValue: Double,
  ): ConfigValue[_] =
    config
      .addDouble(
        key,
        value,
        v =>
          Option(v.doubleValue()).pipe(n => data.copy(value = n)).pipe(upsert),
        defaultValue,
        data.min.getOrElse(noneValue),
        data.max.getOrElse(maxValue),
      )

  override protected def addMinMaxConfig(
    config: ConfigGroup,
    name: String,
    value: Double,
    f: Option[Double] => Unit,
  ): Option[ConfigValue[_]] =
    Option(
      config.addDouble(
        name,
        value,
        v =>
          Option(v.doubleValue())
            .filterNot(_ == noneValue)
            .filterNot(_ == maxValue)
            .pipe(f),
        noneValue,
        noneValue,
        Double.MaxValue,
      ),
    )
}
