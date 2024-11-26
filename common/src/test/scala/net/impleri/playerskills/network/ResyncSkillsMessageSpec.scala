package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.{MessageType => ArchMessageType}
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.slab.chat.StaticText
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.{FriendlyBuffer, MessageType}
import net.minecraft.network.FriendlyByteBuf

class ResyncSkillsMessageSpec extends BaseSpec {
  private val messageTypeMock = mock[MessageType]

  private val serverStateMock = mock[ServerStateContainer]
  private val loggerMock = mock[Logger]

  private val testMessage = ResyncSkillsMessage(Option(serverStateMock), messageTypeMock, loggerMock)
  private val testFactory = ResyncSkillsMessageFactory(Option(serverStateMock), loggerMock)

  private val packetContextMock = mock[NetworkManager.PacketContext]
  private val playerMock = mock[Player]
  private val rawBufferMock = mock[FriendlyByteBuf]
  private val bufferMock = mock[FriendlyBuffer]

  private val underlyingMessageType = mock[ArchMessageType]
  messageTypeMock.value returns underlyingMessageType

  "ResyncSkillsMessage.getType" should "return the messageType" in {
    testMessage.getType should be(underlyingMessageType)
  }

  "ResyncSkillsMessage.write" should "create the right buffer" in {
    testMessage.write(rawBufferMock)
  }

  "ResyncSkillsMessage.handle" should "triggers player resync if there is a server" in {
    val netHandlerMock = mock[NetHandler]

    serverStateMock.getNetHandler returns netHandlerMock

    testMessage.onReceive(Option(playerMock))

    serverStateMock.getNetHandler wasCalled once

    netHandlerMock.syncPlayer(playerMock) wasCalled once
  }

  "ResyncSkillsMessageFactory.receive" should "throw an error if sending without a message type" in {
    bufferMock.nonEmpty returns false

    testFactory.receive(bufferMock).isEmpty shouldBe true
  }

  it should "returns a new message if there is a message type" in {
    bufferMock.nonEmpty returns true

    testFactory.setMessageType(messageTypeMock.value)

    val response = testFactory.receive(bufferMock)

    loggerMock.error(*) wasNever called

    response.value.isInstanceOf[ResyncSkillsMessage] should be(true)
  }

  "ResyncSkillsMessageFactory.send" should "throw an error if sending without a message type" in {
    testFactory.send() shouldBe None
  }

  it should "returns a new message if there is a message type" in {
    testFactory.setMessageType(messageTypeMock.value)

    val response = testFactory.send()

    loggerMock.error(*) wasNever called

    response.value.isInstanceOf[ResyncSkillsMessage] should be(true)
  }

  "ResyncSkillsMessageFactory.apply" should "creates a valid class" in {
    ResyncSkillsMessageFactory().isInstanceOf[ResyncSkillsMessageFactory] should be(true)
  }
}
