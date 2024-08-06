package net.impleri.slab.network

import io.netty.buffer.ByteBuf
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID

case class FriendlyBuffer(private var underlying: FriendlyBuffer.Vanilla) {
  def output: FriendlyBuffer.Vanilla = underlying

  private def chain(f: => FriendlyBuffer.VanillaBase): FriendlyBuffer = {
    val next = new FriendlyBuffer.Vanilla(f)
    copy(underlying = next)
  }

  def readBoolean(): Option[Boolean] = Option(underlying.readBoolean())

  def writeBoolean(value: Boolean): FriendlyBuffer = chain(
    underlying.writeBoolean(value),
  )

  def readInt(): Option[Int] = Option(underlying.readInt())

  def writeInt(value: Int): FriendlyBuffer = chain(underlying.writeInt(value))

  def readDouble(): Option[Double] = Option(underlying.readDouble())

  def writeDouble(value: Double): FriendlyBuffer = chain(
    underlying.writeDouble(value),
  )

  def readUUID(): Option[UUID] = Option(underlying.readUUID())

  def writeUUID(value: UUID): FriendlyBuffer = chain(
    underlying.writeUUID(value),
  )

  def readString(): Option[String] = {
    Option(underlying.readInt())
      .map(underlying.readUtf(_))
      .flatMap(Option(_))
  }

  def writeResourceLocation(value: ResourceLocation): FriendlyBuffer = {
    writeString(value.asString)
  }

  def readResourceLocation(): Option[ResourceLocation] = {
    readString()
      .flatMap(ResourceLocation(_))
  }

  def writeString(value: String): FriendlyBuffer = {
    underlying.writeInt(value.length)
    chain(underlying.writeUtf(value, value.length))
  }

  def readStrings(): Seq[String] = {
    val size = readInt().getOrElse(0)

    (1 to size).toList
      .flatMap(_ => readString())
  }

  def writeStrings(value: Seq[String]): FriendlyBuffer = {
    val next = underlying.writeInt(value.size)
    value.map(writeString).lastOption.getOrElse(next)
  }
}

object FriendlyBuffer {
  type Vanilla = FriendlyByteBuf
  type VanillaBase = ByteBuf
}
