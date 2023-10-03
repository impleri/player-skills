package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.BaseSpec

import java.io.File
import java.util.UUID

class SkillStorageSpec extends BaseSpec {
  "SkillStorage.read" should "return file contents" in {
    val returnedContents = List("file-contents")
    val storageMock = mock[PersistentStorage]
    val resourceMock = mock[SkillResourceFile]
    val file = new File("/tmp")

    val uuid = UUID.randomUUID()

    resourceMock.getPlayerFile(uuid) returns file
    storageMock.read(file) returns Right(returnedContents)

    val target = new SkillStorage(storageMock, resourceMock)

    val received = target.read(uuid)

    received.value should be (returnedContents)
  }

  "SkillStorage.write" should "return success" in {
    val storageMock = mock[PersistentStorage]
    val resourceMock = mock[SkillResourceFile]
    val file = new File("/tmp")

    val uuid = UUID.randomUUID()

    resourceMock.getPlayerFile(uuid) returns file
    storageMock.write(file, *) returns Right(true)

    val target = new SkillStorage(storageMock, resourceMock)

    val received = target.write(uuid, List("skills"))

    received.value should be (true)
  }
}
