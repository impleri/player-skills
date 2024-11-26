package net.impleri.slab.nbt

import net.impleri.slab.resources.ResourceLocation
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

import java.io.File
import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

/**
 * Nbt Contents
 *
 * Immutable wrapper to NBT Tag handling. We stay immutable by tracking changes immutably without mutating the underlying
 * NBT data until needed.
 * Note that if you are doing write operations to NBT and not using `writeToFile` or `raw`, you may need to use `commit`
 * to mutate the underlying NBT. Some mods may invoke writes and expect the NBT passed to be mutated rather than returned.
 */
case class NbtContents(private val underlying: CompoundTag, changes: Seq[() => Unit] = Seq.empty) {
  def output: CompoundTag = {
    commit().underlying
  }

  def commit(): NbtContents = {
    changes.foreach(_())
    copy(underlying = underlying, changes = Seq.empty)
  }

  def writeToFile(file: File): Either[NbtFileWriteError, Unit] =
    Try(NbtIo.writeCompressed(output, file)).toOption
      .toRight(FailedToWrite(file))

  private def upsert(key: String, value: Tag): NbtContents = {
    val nextChange: () => Unit = () => {
      underlying.put(key, value)
    }

    copy(underlying = underlying, changes = changes :+ nextChange)
  }

  private def readList(key: String, tagType: Int): List[Tag] =
    Try(underlying.getList(key, tagType)).toOption.toList
      .flatMap(_.asScala.toList)

  private def createListTag(values: Seq[String]): ListTag =
    new ListTag()
      .tap(
        _.addAll(
          values.map(StringTag.valueOf).asJava,
        ),
      )

  def getBoolean(key: String): Option[Boolean] =
    Try(underlying.getBoolean(key)).toOption

  def putBoolean(key: String, value: Boolean): NbtContents =
    ByteTag
      .valueOf(value)
      .pipe(upsert(key, _))

  def getDouble(key: String): Option[Double] =
    Try(underlying.getDouble(key)).toOption

  def putDouble(key: String, value: Double): NbtContents =
    DoubleTag
      .valueOf(value)
      .pipe(upsert(key, _))

  def getString(key: String): Option[String] =
    Try(underlying.getString(key)).toOption
      .filter(_.nonEmpty)

  def putString(key: String, value: String): NbtContents =
    StringTag
      .valueOf(value)
      .pipe(upsert(key, _))

  def getResourceLocation(key: String): Option[ResourceLocation] =
    getString(key).flatMap(ResourceLocation(_))

  def getStrings(key: String): List[String] =
    readList(key, Tag.TAG_STRING.toInt)
      .map(_.getAsString)

  def putStrings(key: String, values: Seq[String]): NbtContents =
    createListTag(values)
      .pipe(upsert(key, _))
}

object NbtContents {
  def apply(): NbtContents = new NbtContents(new CompoundTag())

  def fromFile(file: File): Either[NbtFileReadError, NbtContents] =
    Try(NbtIo.readCompressed(file))
      .toOption
      .map(new NbtContents(_))
      .toRight(NbtFileMissing(file))
}
