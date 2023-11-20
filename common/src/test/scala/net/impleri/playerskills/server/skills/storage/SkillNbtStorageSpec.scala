package net.impleri.playerskills.server.skills.storage

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.NbtIO
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag

import java.io.File
import java.io.FileNotFoundException
import scala.jdk.javaapi.CollectionConverters
import scala.util.chaining.scalaUtilChainingOps

class SkillNbtStorageSpec extends BaseSpec {
  private val nbtMock = mock[NbtIO]
  private val nbtStorage = SkillNbtStorage(nbtMock)
  private val testFile = new File("/temp/testFile")

  private val badCompoundTag = new CompoundTag()
    .tap(_.put("test", StringTag.valueOf("test-file")))

  private val testSkills = List("1", "2", "everything")

  private val readSkills = testSkills.map(StringTag.valueOf)
    .pipe(CollectionConverters.asJava(_))

  private val skillsList = new ListTag()
    .tap(_.addAll(readSkills))

  private val goodCompoundTag = new CompoundTag()
    .tap(_.put(SkillNbtStorage.SKILLS_TAG, skillsList))

  "SkillNbtStorageSpec.read" should "overload IO exceptions" in {
    nbtMock.read(testFile) returns Left(new FileNotFoundException)

    nbtStorage.read(testFile).left.value should be(SkillFileMissing(testFile))
  }

  it should "throw an error if the skills tag is not present" in {
    nbtMock.read(testFile) returns Right(badCompoundTag)

    nbtStorage.read(testFile).left.value should be(SkillFileHasNoData())
  }

  it should "return the unpacked strings from NBT" in {
    nbtMock.read(testFile) returns Right(goodCompoundTag)

    nbtStorage.read(testFile).value should be(testSkills)
  }

  "SkillNbtStorageSpec.write" should "overload IO exceptions" in {
    nbtMock.write(*, *) returns Left(new FileNotFoundException)

    nbtStorage.write(testFile, testSkills).left.value should be(FailedToWrite(testFile))
  }

  it should "save the NBT file" in {
    nbtMock.write(testFile, goodCompoundTag) returns Right(())

    nbtStorage.write(testFile, testSkills).value should be(true)
  }
}
