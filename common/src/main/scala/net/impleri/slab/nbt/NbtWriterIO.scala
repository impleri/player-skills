package net.impleri.slab.nbt

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

import java.io.File
import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

case class NbtWriterIO(private val file: File, private val contents: Option[CompoundTag] = None) {
  private def write(tag: CompoundTag): Either[NbtFileWriteError, CompoundTag] = {
    Try(NbtIo.writeCompressed(tag, file))
      .toOption
      .toRight(FailedToWrite(file))
      .map(_ => tag)
  }

  private def upsertContents(key: String, value: Tag): CompoundTag = {
    contents.getOrElse(new CompoundTag())
      .tap(_.put(key, value))
  }

  private def write(key: String, tag: Tag): Either[NbtFileWriteError, CompoundTag] = {
    upsertContents(key, tag)
      .pipe(write)
  }

  private def createListTag(values: Seq[String]): ListTag = {
    new ListTag()
      .tap(
        _.addAll(
          values.map(StringTag.valueOf).asJava,
        ),
      )
  }

  def updateStrings(key: String, values: Seq[String]): Either[NbtFileWriteError, CompoundTag] = {
    createListTag(values)
      .pipe(write(key, _))
  }
}
