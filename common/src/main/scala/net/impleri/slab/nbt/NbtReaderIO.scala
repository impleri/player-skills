package net.impleri.slab.nbt

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag

import java.io.File
import scala.jdk.CollectionConverters._
import scala.util.Try

case class NbtReaderIO(private val file: File) {
  private def read(): Either[NbtFileReadError, CompoundTag] = {
    Try(NbtIo.readCompressed(file))
      .toOption
      .toRight(NbtFileMissing(file))
  }

  private def read(key: String): Either[NbtFileReadError, CompoundTag] = {
    read()
      .filterOrElse(_.contains(key), NbtFileMissingData())
  }

  private def safeGetList(key: String, tagType: Int)(contents: CompoundTag): Either[NbtFileReadFailed, ListTag] = {
    Try(contents.getList(key, tagType))
      .toOption
      .toRight(NbtFileReadFailed(file))
  }

  private def readList(key: String, tagType: Int): Either[NbtFileReadError, List[Tag]] = {
    read(key)
      .flatMap(safeGetList(key, tagType))
      .map(_.asScala.toList)
  }

  def readListAsString(key: String): Either[NbtFileReadError, List[String]] = {
    readList(key, Tag.TAG_STRING.toInt)
      .map(_.map(_.getAsString))
  }
}
