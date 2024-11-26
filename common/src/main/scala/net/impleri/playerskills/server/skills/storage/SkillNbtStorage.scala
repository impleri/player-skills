package net.impleri.playerskills.server.skills.storage

import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.nbt.NbtFileReadError
import net.impleri.slab.nbt.NbtFileWriteError

import java.io.File

/** Save data in NBT format
  */
case class SkillNbtStorage private[skills] () {
  def read(file: File): Either[NbtFileReadError, List[String]] =
    NbtContents
      .fromFile(file)
      .map(_.getStrings(SkillNbtStorage.SKILLS_TAG))

  def write(
    file: File,
    skills: List[String],
  ): Either[NbtFileWriteError, Boolean] =
    NbtContents
      .fromFile(file)
      .getOrElse(NbtContents())
      .putStrings(SkillNbtStorage.SKILLS_TAG, skills)
      .writeToFile(file)
      .map(_ => true)
}

object SkillNbtStorage {
  private[storage] val SKILLS_TAG: String = "acquiredSkills"
}
