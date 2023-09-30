package net.impleri.playerskills.skills.registry.storage

import java.io.File

trait PersistentStorage {
  def read(file: File): Either[NbtFileReadError, List[String]]

  def write(file: File, skills: List[String]): Either[NbtFileWriteError, Boolean]
}
