package net.impleri.playerskills.client

import dev.architectury.networking.simple.BaseC2SMessage
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.MinecraftClient
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.player.LocalPlayer

import java.util.UUID


class NetHandlerSpec extends BaseSpec {
  "NetHandler.resyncPlayer" should "send a request to the server" in {
    val clientMock = mock[MinecraftClient]
    val loggerMock = mock[PlayerSkillsLogger]
    val playerMock = mock[MinecraftPlayer[LocalPlayer]]
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid
    clientMock.getPlayer returns playerMock

    val testUnit = NetHandler(clientMock, loggerMock)

    testUnit.resyncPlayer()

    loggerMock.debug(*) wasCalled once
    playerMock.sendMessage(any[BaseC2SMessage]) wasCalled once
  }

  "NetHandler.apply" should "return a usable instance" in {
    val unit = NetHandler()

    unit.isInstanceOf[NetHandler] should be(true)
  }
}
