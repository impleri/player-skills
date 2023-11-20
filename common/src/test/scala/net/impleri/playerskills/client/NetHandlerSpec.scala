package net.impleri.playerskills.client

import dev.architectury.networking.simple.BaseC2SMessage
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.client.player.LocalPlayer

import java.util.UUID


class NetHandlerSpec extends BaseSpec {
  "NetHandler.resyncPlayer" should "send a request to the server" in {
    val clientMock = mock[Client]
    val loggerMock = mock[PlayerSkillsLogger]
    val playerMock = mock[Player[LocalPlayer]]
    val messageFactoryMock = mock[ResyncSkillsMessageFactory]
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid
    clientMock.getPlayer returns playerMock

    val testUnit = NetHandler(clientMock, messageFactoryMock, loggerMock)

    testUnit.resyncPlayer()

    loggerMock.debug(*) wasCalled once
    playerMock.sendMessage(any[BaseC2SMessage]) wasCalled once
  }

  "NetHandler.apply" should "return a usable instance" in {
    val unit = NetHandler(messageFactory = mock[ResyncSkillsMessageFactory])

    unit.isInstanceOf[NetHandler] should be(true)
  }
}
