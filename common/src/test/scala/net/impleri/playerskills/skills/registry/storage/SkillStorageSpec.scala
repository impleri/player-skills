package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.BaseSpec
import net.minecraft.server.MinecraftServer

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
}
