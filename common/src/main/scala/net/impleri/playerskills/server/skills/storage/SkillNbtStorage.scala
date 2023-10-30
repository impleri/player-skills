package net.impleri.playerskills.server.skills.storage

import net.impleri.playerskills.facades.MinecraftNbtIO
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

import java.io.File
import java.util.{List => JavaList}
import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

sealed trait ReadNbtSkills {
  protected def mcNbt: MinecraftNbtIO

  protected def readFile(file: File): Either[SkillFileMissing, CompoundTag] = {
    mcNbt.read(file)
      .left
      .map(_ => SkillFileMissing(file))
  }

  protected def getList(tag: CompoundTag): Either[SkillFileHasNoData, ListTag] = {
    if (tag.contains(SkillNbtStorage.SKILLS_TAG)) {
      Right(tag.getList(SkillNbtStorage.SKILLS_TAG, Tag.TAG_STRING.toInt))
    } else {
      Left(SkillFileHasNoData())
    }
  }
}

sealed trait WriteNbtSkills {
  protected def mcNbt: MinecraftNbtIO

  protected def convertSkills(skills: List[String]): JavaList[StringTag] = {
    skills.map(StringTag.valueOf)
      .pipe(CollectionConverters.asJava(_))
  }

  protected def createSkillList(skillsList: JavaList[StringTag]): ListTag = {
    new ListTag()
      .tap(_.addAll(skillsList))
  }

  protected def createCompoundTag(skillListTag: ListTag): CompoundTag = {
    new CompoundTag()
      .tap(_.put(SkillNbtStorage.SKILLS_TAG, skillListTag))
  }

  protected def tryWrite(file: File)(tag: CompoundTag): Either[NbtFileWriteError, Boolean] = {
    mcNbt.write(file, tag)
      .map(_ => true)
      .left.map(_ => FailedToWrite(file))
  }
}

/**
 * Save data in NBT format
 */
class SkillNbtStorage private[skills] (override val mcNbt: MinecraftNbtIO)
  extends PersistentStorage with ReadNbtSkills with WriteNbtSkills {
  override def read(file: File): Either[NbtFileReadError, List[String]] = {
    for {
      rawFile <- readFile(file)
      skills <- getList(rawFile)
    } yield CollectionConverters.asScala(skills.iterator)
      .toList
      .map(_.getAsString)
  }

  override def write(file: File, skills: List[String]): Either[NbtFileWriteError, Boolean] = {
    convertSkills(skills)
      .pipe(createSkillList)
      .pipe(createCompoundTag)
      .pipe(tryWrite(file))
  }
}

object SkillNbtStorage {
  private[storage] val SKILLS_TAG: String = "acquiredSkills"

  // package-private as this should be accessed through SkillStorage
  private[skills] def apply(mcNbt: MinecraftNbtIO = MinecraftNbtIO()) = {
    new SkillNbtStorage(mcNbt)
  }
}
