package net.impleri.slab.network

import io.netty.buffer.ByteBuf
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.network.FriendlyByteBuf

import java.util.UUID
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

trait FriendlyBufferBase {
  protected def underlying: FriendlyBuffer.Vanilla

  protected def upsert(buffer: FriendlyBuffer.VanillaBase): FriendlyBuffer = ???
}

trait FriendlyBoolean extends FriendlyBufferBase {
  def readBoolean(): Option[Boolean] = Try(underlying.readBoolean())
    .toOption

  def writeBoolean(value: Boolean): FriendlyBuffer = upsert(
    underlying.writeBoolean(value),
  )
}

trait FriendlyNumber extends FriendlyBufferBase {
  def readInt(): Option[Int] = Try(underlying.readInt()).toOption

  def writeInt(value: Int): FriendlyBuffer = upsert(underlying.writeInt(value))

  def readDouble(): Option[Double] = Try(underlying.readDouble()).toOption

  def writeDouble(value: Double): FriendlyBuffer = upsert(
    underlying.writeDouble(value),
  )
}

trait FriendlyString extends FriendlyNumber {
  def readString(): Option[String] =
    Try(underlying.readInt())
      .filter(_ >= 0)
      .flatMap(l => Try(underlying.readUtf(l)))
      .toOption

  private def writeStringInternal(value: String): ByteBuf = {
    underlying.writeInt(value.length)
    underlying.writeUtf(value, value.length)
  }

  def writeString(value: String): FriendlyBuffer =
    upsert(writeStringInternal(value))

  private def readStringsInternal(size: Int = 0): Seq[String] =
    (0 until size)
      .toList
      .flatMap(_ => readString())

  def readStrings(): Seq[String] =
    readInt()
      .filter(_ > 0)
      .map(readStringsInternal)
      .getOrElse(Seq.empty)

  def writeStrings(value: Seq[String]): FriendlyBuffer = {
    val trimmed = value.filter(_.nonEmpty)
    val next = underlying.writeInt(trimmed.size)

    trimmed
      .map(writeStringInternal)
      .lastOption
      .getOrElse(next)
      .pipe(upsert)
  }
}

trait FriendlyIdentifier extends FriendlyString {
  def readUUID(): Option[UUID] = Try(underlying.readUUID()).toOption

  def writeUUID(value: UUID): FriendlyBuffer = upsert(
    underlying.writeUUID(value),
  )

  def readResourceLocation(): Option[ResourceLocation] =
    readString()
      .flatMap(ResourceLocation(_))

  def writeResourceLocation(value: ResourceLocation): FriendlyBuffer =
    writeString(value.asString)
}

case class FriendlyBuffer(override val underlying: FriendlyBuffer.Vanilla)
  extends FriendlyBufferBase with FriendlyBoolean with FriendlyNumber with FriendlyString with FriendlyIdentifier {
  def output: FriendlyBuffer.Vanilla = underlying

  def nonEmpty: Boolean = underlying.isReadable

  override def upsert(bb: FriendlyBuffer.VanillaBase): FriendlyBuffer =
    bb
      .pipe(new FriendlyBuffer.Vanilla(_))
      .pipe(b => copy(underlying = b))
}

object FriendlyBuffer {
  type Vanilla = FriendlyByteBuf
  type VanillaBase = ByteBuf
}
