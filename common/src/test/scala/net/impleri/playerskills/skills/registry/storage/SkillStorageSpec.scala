package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.BaseSpec
import net.minecraft.server.MinecraftServer

import java.io.File
import java.nio.file.Path
import java.util.UUID

class SkillStorageSpec extends BaseSpec {
  "SkillStorage.setup" should "prepare SkillResourceFile" in {
    val serverMock = mock[MinecraftServer]
    val returnedPath = mock[Path]

    serverMock.getWorldPath(*) returns returnedPath

    SkillStorage.setup(serverMock)

    SkillResourceFile.instance.value.storage should be (returnedPath)
  }

  "SkillStorage.cleanup" should "clean SkillResourceFile" in {
    val pathMock = mock[Path]

    SkillResourceFile.instance = Some(new SkillResourceFile(pathMock))

    SkillStorage.cleanup()

    SkillResourceFile.instance should be (None)
  }

  "SkillStorage.read" should "error if server hasn't been set up" in {
    val storageMock = mock[PersistentStorage]

    val target = new SkillStorage(storageMock)
    val uuid = UUID.randomUUID()

    val received = target.read(uuid)

    received.left.value should be (ReadBeforeServerLoaded())
  }

  it should "return file contents" in {
    val returnedContents = List("file-contents")
    val storageMock = mock[PersistentStorage]
    val resourceMock = mock[SkillResourceFile]
    val file = new File("/tmp")

    val uuid = UUID.randomUUID()

    resourceMock.getPlayerFile(uuid) returns file
    storageMock.read(file) returns Right(returnedContents)

    SkillResourceFile.instance = Some(resourceMock)
    val target = new SkillStorage(storageMock)

    val received = target.read(uuid)

    received.value should be (returnedContents)
  }

  "SkillStorage.write" should "error if server hasn't been set up" in {
    val storageMock = mock[PersistentStorage]

    val target = SkillStorage(storageMock)
    val uuid = UUID.randomUUID()

    SkillResourceFile.instance = None
    val received = target.write(uuid, List("skills"))

    received.left.value should be (WriteBeforeServerLoaded())
  }

  it should "return success" in {
    val storageMock = mock[PersistentStorage]
    val resourceMock = mock[SkillResourceFile]
    val file = new File("/tmp")

    val uuid = UUID.randomUUID()

    resourceMock.getPlayerFile(uuid) returns file
    storageMock.write(file, *) returns Right(true)

    SkillResourceFile.instance = Some(resourceMock)
    val target = new SkillStorage(storageMock)

    val received = target.write(uuid, List("skills"))

    received.value should be (true)
  }
}
