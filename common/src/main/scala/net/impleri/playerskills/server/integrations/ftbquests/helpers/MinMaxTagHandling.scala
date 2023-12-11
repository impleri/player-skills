package net.impleri.playerskills.server.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf

trait MinMaxTagHandling[T] extends DowngradeTagHandling[T] {
  protected def skillTypeOps: SkillTypeOps

  protected def playerOps: PlayerOps

  protected var min: Option[T] = None
  protected var max: Option[T] = None

  protected def writeMinMaxTag(nbt: CompoundTag, key: String, value: Option[T]): Unit = ???

  protected def writeMinMaxTags(nbt: CompoundTag): Unit = {
    writeMinMaxTag(nbt, "min", min)
    writeMinMaxTag(nbt, "max", max)
  }

  protected def readMinMaxTag(nbt: CompoundTag, key: String): Option[T] = ???

  protected def readMinMaxTags(nbt: CompoundTag): Unit = {
    min = readMinMaxTag(nbt, "min")
    max = readMinMaxTag(nbt, "max")
  }

  protected def writeMinMaxBuffer(buffer: FriendlyByteBuf, value: Option[T]): Unit = ???

  protected def writeMinMaxBuffers(buffer: FriendlyByteBuf): Unit = {
    writeMinMaxBuffer(buffer, min)
    writeMinMaxBuffer(buffer, max)
  }

  protected def readMinMaxBuffer(buffer: FriendlyByteBuf): Option[T] = ???

  protected def readMinMaxBuffers(buffer: FriendlyByteBuf): Unit = {
    min = readMinMaxBuffer(buffer)
    max = readMinMaxBuffer(buffer)
  }

  protected def addMinMaxToConfig(config: ConfigGroup): Unit = ???
}
