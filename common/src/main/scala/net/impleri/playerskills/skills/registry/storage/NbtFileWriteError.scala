package net.impleri.playerskills.skills.registry.storage

import java.io.File

trait NbtFileWriteError

case class FailedToWrite(file: File) extends Exception(s"Player data file ${file.getPath} could not be written") with NbtFileWriteError

case class WriteBeforeServerLoaded() extends  Exception("Minecraft Server not loaded while attempting to write a file") with NbtFileWriteError
