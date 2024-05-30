package net.impleri.playerskills.server.skills.storage

import net.impleri.slab.nbt.NbtFileReadError
import net.impleri.slab.nbt.NbtFileWriteError
import net.impleri.slab.nbt.NbtReaderIO
import net.impleri.slab.nbt.NbtWriterIO

import java.io.File

/**
 * Save data in NBT format
 */
case class SkillNbtStorage private[skills] () {
  def read(file: File): Either[NbtFileReadError, List[String]] = {
    NbtReaderIO(file).readListAsString(SkillNbtStorage.SKILLS_TAG)
  }

  def write(file: File, skills: List[String]): Either[NbtFileWriteError, Boolean] = {
    NbtWriterIO(file).updateStrings(SkillNbtStorage.SKILLS_TAG, skills).map(_ => true)
  }
}

object SkillNbtStorage {
  private[storage] val SKILLS_TAG: String = "acquiredSkills"
}
