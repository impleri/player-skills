package net.impleri.playerskills.network

import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.NetworkManager
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

import java.util.UUID

class ResyncSkillsMessageSpec extends BaseSpec {
  private val messageTypeMock = mock[MessageType]

  private val serverStateMock = mock[ServerStateContainer]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUuid = UUID.randomUUID()

  private val testMessage = ResyncSkillsMessage(testUuid, Option(serverStateMock), messageTypeMock)
  private val testFactory = ResyncSkillsMessageFactory(Option(serverStateMock), loggerMock)

  private val packetContextMock = mock[NetworkManager.PacketContext]
  private val playerMock = mock[Player[ServerPlayer]]
  private val bufferMock = mock[FriendlyByteBuf]

  "ResyncSkillsMessage.getType" should "return the messageType" in {
    testMessage.getType should be(messageTypeMock)
  }

  "ResyncSkillsMessage.write" should "create the right buffer" in {
    testMessage.write(bufferMock)

    bufferMock.writeUUID(testUuid) wasCalled once
  }

  "ResyncSkillsMessage.handle" should "does nothing if there is no server" in {
    serverStateMock.SERVER returns None

    val netHandlerMock = mock[NetHandler]

    serverStateMock.getNetHandler returns netHandlerMock

    testMessage.handle(packetContextMock)

    netHandlerMock.syncPlayer(*) wasNever called
  }

  it should "triggers player resync if there is a server" in {
    val serverMock = mock[Server]
    val netHandlerMock = mock[NetHandler]

    serverStateMock.SERVER returns Option(serverMock)
    serverStateMock.getNetHandler returns netHandlerMock

    serverMock.getPlayer(testUuid) returns Option(playerMock)

    testMessage.handle(packetContextMock)

    serverStateMock.getNetHandler wasCalled once

    netHandlerMock.syncPlayer(playerMock) wasCalled once
  }

  "ResyncSkillsMessageFactory.receive" should "throw an error if sending without a message type" in {
    val givenUuid = UUID.randomUUID()

    bufferMock.readUUID() returns givenUuid

    assertThrows[Throwable] {
      testFactory.receive(bufferMock)
    }

    loggerMock.error(*) wasCalled once
  }

  it should "returns a new message if there is a message type" in {
    val givenUuid = UUID.randomUUID()

    bufferMock.readUUID() returns givenUuid

    testFactory.setMessageType(messageTypeMock)

    val response = testFactory.receive(bufferMock)

    loggerMock.error(*) wasNever called

    response.isInstanceOf[ResyncSkillsMessage] should be(true)
  }

  "ResyncSkillsMessageFactory.send" should "throw an error if sending without a message type" in {
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid

    assertThrows[Throwable] {
      testFactory.send(playerMock)
    }

    loggerMock.error(*) wasCalled once
  }

  it should "returns a new message if there is a message type" in {
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid

    testFactory.setMessageType(messageTypeMock)

    val response = testFactory.send(playerMock)

    loggerMock.error(*) wasNever called

    response.isInstanceOf[ResyncSkillsMessage] should be(true)
  }

  "ResyncSkillsMessageFactory.apply" should "creates a valid class" in {
    ResyncSkillsMessageFactory().isInstanceOf[ResyncSkillsMessageFactory] should be(true)
  }
}
