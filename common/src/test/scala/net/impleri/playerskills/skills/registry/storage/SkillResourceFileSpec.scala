package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.BaseSpec
import net.minecraft.server.MinecraftServer

import java.io.File
import java.nio.file.Path
import java.util.UUID

class SkillResourceFileSpec extends BaseSpec {
  "SkillResourceFile.createInstance" should "create an instance" in {
    val serverMock = mock[MinecraftServer]
    val returnedPath = mock[Path]

    serverMock.getWorldPath(*) returns returnedPath

    SkillResourceFile.createInstance(serverMock)

    SkillResourceFile.instance.value.storage should be (returnedPath)
  }

  "SkillResourceFile.destroyInstance" should "remove the instance" in {
    val pathMock = mock[Path]

    SkillResourceFile.instance = Some(new SkillResourceFile(pathMock))

    SkillResourceFile.destroyInstance()

    SkillResourceFile.instance should be (None)
  }

  "SkillResourceFile.forPlayer" should "return nothing if server not started" in {
    val testId = UUID.randomUUID()
    SkillResourceFile.forPlayer(testId) should be (None)
  }

  it should "return a path if server started" in {
    val testId = UUID.randomUUID()
    val pathMock = mock[Path]
    val fileMock = new File("/tmp")

    pathMock.toFile returns fileMock

    SkillResourceFile.instance = Some(new SkillResourceFile(pathMock))

    SkillResourceFile.forPlayer(testId).map(_.toString) should be (Some(s"/tmp/players/${testId.toString}.skills"))
  }
}
