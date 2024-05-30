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

case class NbtContents(private val underlying: CompoundTag) {
  def writeToFile(file: File): Either[NbtFileWriteError, Unit] = {
    Try(NbtIo.writeCompressed(underlying, file))
      .toOption
      .toRight(FailedToWrite(file))
  }

  private def upsert(key: String, value: Tag): NbtContents = {
    val next = underlying.copy()
      .tap(_.put(key, value))
    copy(underlying = next)
  }

  private def createListTag(values: Seq[String]): ListTag = {
    new ListTag()
      .tap(
        _.addAll(
          values.map(StringTag.valueOf).asJava,
        ),
      )
  }

  def getBoolean(key: String): Option[Boolean] = {
    Try(underlying.getBoolean(key))
      .toOption
  }

  def getDouble(key: String): Option[Double] = {
    Try(underlying.getDouble(key))
      .toOption
  }

  def getString(key: String): Option[String] = {
    Try(underlying.getString(key))
      .toOption
      .filter(_.nonEmpty)
  }

  def putBoolean(key: String, value: Boolean): NbtContents = {
    ByteTag.valueOf(value)
      .pipe(upsert(key, _))
  }

  def putDouble(key: String, value: Double): NbtContents = {
    DoubleTag.valueOf(value)
      .pipe(upsert(key, _))
  }

  def putString(key: String, value: String): NbtContents = {
    StringTag.valueOf(value)
      .pipe(upsert(key, _))
  }

  def getResourceLocation(key: String): Option[ResourceLocation] = {
    getString(key).flatMap(ResourceLocation(_))
  }

  def putStrings(key: String, values: Seq[String]): NbtContents = {
    createListTag(values)
      .pipe(upsert(key, _))
  }
}
