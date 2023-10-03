package net.impleri.playerskills.skills.registry.storage

import net.impleri.playerskills.BaseSpec
import net.minecraft.server.MinecraftServer

import java.io.File
import java.nio.file.Path
import java.util.UUID

class SkillResourceFileSpec extends BaseSpec {
  "SkillResourceFile.forPlayer" should "return a path if server started" in {
    val testId = UUID.randomUUID()
    val pathMock = mock[Path]
    val fileMock = new File("/tmp")

    pathMock.toFile returns fileMock

    val unit= SkillResourceFile(pathMock)

    unit.getPlayerFile(testId).toString should be (s"/tmp/players/${testId.toString}.skills")
  }
}
