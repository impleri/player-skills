package net.impleri.playerskills.server.skills.storage

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.MinecraftServer

import java.io.File
import java.nio.file.Path
import java.util.UUID

class SkillResourceFileSpec extends BaseSpec {
  "SkillResourceFile.getPlayerFile" should "return a path if server started" in {
    val testId = UUID.randomUUID()
    val pathMock = mock[Path]
    val fileMock = new File("/tmp")

    pathMock.toFile returns fileMock

    val unit = SkillResourceFile(pathMock)

    unit.getPlayerFile(testId).toString should be(s"/tmp/players/${testId.toString}.skills")
  }

  "SkillStorage.apply" should "create the right class" in {
    val serverMock = mock[MinecraftServer]
    val pathMock = mock[Path]

    serverMock.getWorldPath(*) returns pathMock

    val received = SkillResourceFile(serverMock)

    received.storage should be(pathMock)
  }
}
