package net.impleri.slab.nbt

import java.io.File

sealed trait NbtFileWriteError

case class FailedToWrite(file: File)
    extends Exception(s"Player data file ${file.getPath} could not be written")
    with NbtFileWriteError
