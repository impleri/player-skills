package net.impleri.playerskills.server.skills.storage

import java.io.File

sealed trait NbtFileReadError

case class SkillFileMissing(file: File)
  extends Exception(s"Player data file ${file.getPath} does not exist") with NbtFileReadError

case class SkillFileHasNoData() extends Exception("Player file missing skills data") with NbtFileReadError
