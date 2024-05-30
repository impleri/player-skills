package net.impleri.slab.nbt

import java.io.File

sealed trait NbtFileReadError

case class NbtFileMissing(file: File)
  extends Exception(s"Player data file ${file.getPath} does not exist") with NbtFileReadError

case class NbtFileReadFailed(file: File)
  extends Exception(s"Player data file ${file.getPath} may be corrupted") with NbtFileReadError

case class NbtFileMissingData() extends Exception("Player file missing expected data") with NbtFileReadError
