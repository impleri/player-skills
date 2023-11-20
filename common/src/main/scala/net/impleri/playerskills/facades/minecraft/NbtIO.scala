package net.impleri.playerskills.facades.minecraft

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo

import java.io.File
import scala.util.Try

// Facade to Minecraft class for dependency injection
case class NbtIO() {
  def read(file: File): Either[Throwable, CompoundTag] = {
    Try(NbtIo.readCompressed(file))
      .toEither
  }

  def write(file: File, tag: CompoundTag): Either[Throwable, Unit] = {
    Try(NbtIo.writeCompressed(tag, file))
      .toEither
  }
}
