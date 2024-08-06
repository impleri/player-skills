package net.impleri.playerskills.server.skills

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.skills.storage.SkillNbtStorage
import net.impleri.playerskills.server.skills.storage.SkillResourceFile
import net.impleri.slab.logging.Logger
import net.impleri.slab.nbt.FailedToWrite
import net.impleri.slab.nbt.NbtFileMissing
import net.impleri.slab.server.Server

import java.io.File
import java.nio.file.Path
import java.util.UUID

class PlayerStorageIOSpec extends BaseSpec {
  private case class TestSkill() extends Skill[String]

  "PlayerStorageIO.read" should "return file contents" in {
    val storageMock = mock[SkillNbtStorage]
    val resourceMock = mock[SkillResourceFile]
    val typeOpsMock = mock[SkillTypeOps]
    val loggerMock = mock[Logger]

    val givenUuid = UUID.randomUUID()

    val file = new File("/tmp")
    resourceMock.getPlayerFile(givenUuid) returns file

    val returnedContents = List("file-contents")
    storageMock.read(file) returns Right(returnedContents)

    val returnedSkills = List(TestSkill())
    typeOpsMock.deserializeAll(returnedContents) returns returnedSkills

    val target =
      new PlayerStorageIO(storageMock, resourceMock, typeOpsMock, loggerMock)

    val received = target.read(givenUuid)

    received should be(returnedSkills)
  }

  "PlayerStorageIO.read" should "return an empty list if the file isn't found" in {
    val storageMock = mock[SkillNbtStorage]
    val resourceMock = mock[SkillResourceFile]
    val typeOpsMock = mock[SkillTypeOps]
    val loggerMock = mock[Logger]

    val givenUuid = UUID.randomUUID()

    val file = new File("/tmp")
    resourceMock.getPlayerFile(givenUuid) returns file

    storageMock.read(file) returns Left(NbtFileMissing(file))

    val target =
      new PlayerStorageIO(storageMock, resourceMock, typeOpsMock, loggerMock)

    val received = target.read(givenUuid)

    received should be(List.empty)
  }

  "PlayerStorageIO.write" should "return success" in {
    val storageMock = mock[SkillNbtStorage]
    val resourceMock = mock[SkillResourceFile]
    val typeOpsMock = mock[SkillTypeOps]
    val loggerMock = mock[Logger]

    val givenUuid = UUID.randomUUID()
    val givenSkills = List(TestSkill())

    val serializedSkill = "test-skill"
    typeOpsMock.serialize(TestSkill()) returns Option(serializedSkill)

    val file = new File("/tmp")
    resourceMock.getPlayerFile(givenUuid) returns file

    storageMock.write(file, List(serializedSkill)) returns Right(true)

    val target =
      new PlayerStorageIO(storageMock, resourceMock, typeOpsMock, loggerMock)

    val received = target.write(givenUuid, givenSkills)

    received should be(true)
  }

  "PlayerStorageIO.write" should "return failure if the write fails" in {
    val storageMock = mock[SkillNbtStorage]
    val resourceMock = mock[SkillResourceFile]
    val typeOpsMock = mock[SkillTypeOps]
    val loggerMock = mock[Logger]

    val givenUuid = UUID.randomUUID()
    val givenSkills = List(TestSkill())

    val serializedSkill = "test-skill"
    typeOpsMock.serialize(TestSkill()) returns Option(serializedSkill)

    val file = new File("/tmp")
    resourceMock.getPlayerFile(givenUuid) returns file

    storageMock.write(file, List(serializedSkill)) returns Left(
      FailedToWrite(file)
    )

    val target =
      new PlayerStorageIO(storageMock, resourceMock, typeOpsMock, loggerMock)

    val received = target.write(givenUuid, givenSkills)

    received should be(false)
  }

  "PlayerStorageIO.apply" should "return the correct class" in {
    val serverMock = mock[Server]
    val skillTypeOpsMock = mock[SkillTypeOps]
    val storageMock = mock[SkillNbtStorage]

    val pathMock = mock[Path]
    serverMock.getWorldPath(*) returns pathMock

    val target = PlayerStorageIO(serverMock, skillTypeOpsMock, storageMock)

    target.skillFile.storage should be(pathMock)
  }
}
