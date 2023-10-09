package net.impleri.playerskills.server.skills.storage

import java.io.File

trait NbtFileWriteError

case class FailedToWrite(file: File)
  extends Exception(s"Player data file ${file.getPath} could not be written") with NbtFileWriteError
