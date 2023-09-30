package net.impleri.playerskills.skills.registry.storage

import java.io.File

sealed trait NbtFileReadError

case class SkillFileMissing(file: File) extends Exception(s"Player data file ${file.getPath} does not exist") with NbtFileReadError

case class SkillFileHasNoData() extends Exception("Player file missing skills data") with NbtFileReadError

case class ReadBeforeServerLoaded() extends  Exception("Minecraft Server not loaded while attempting to read a file") with NbtFileReadError
