package net.impleri.playerskills.network

import dev.architectury.networking.NetworkManager
import dev.architectury.networking.simple.{MessageType => ArchMessageType}
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientStateContainer
import net.impleri.playerskills.client.NetHandler
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.MessageType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

import java.util.UUID

class SyncSkillsMessageSpec extends BaseSpec {
  private val messageTypeMock = mock[MessageType]

  private val skillTypeOpsMock = mock[SkillTypeOps]
  private val clientStateMock = mock[ClientStateContainer]
  private val loggerMock = mock[Logger]

  private val testUuid = UUID.randomUUID()
  private val skill1 = mock[Skill[Boolean]]
  private val skill2 = mock[Skill[String]]
  private val skills = List(skill1, skill2)

  private val testMessage = SyncSkillsMessage(testUuid,
    skills,
    force = false,
    skillTypeOpsMock,
    Option(clientStateMock),
    messageTypeMock,
    loggerMock,
  )
  private val testFactory = SyncSkillsMessageFactory(
    skillTypeOpsMock,
    Option(clientStateMock),
    loggerMock,
  )

  private val packetContextMock = mock[NetworkManager.PacketContext]
  private val playerMock = mock[Player[ServerPlayer]]
  private val bufferMock = mock[FriendlyByteBuf]

  private val underlyingMessageType = mock[ArchMessageType]
  messageTypeMock.value returns underlyingMessageType

  "SyncSkillsMessage.getType" should "return the messageType" in {
    testMessage.getType should be(underlyingMessageType)
  }

  "SyncSkillsMessage.write" should "create the right buffer" in {
    val serializedSkill1 = "testone"
    val serializedSkill2 = "testtwoo"

    bufferMock.writeUUID(testUuid) returns bufferMock
    bufferMock.writeBoolean(false) returns bufferMock

    skillTypeOpsMock.serialize(skill1) returns Option(serializedSkill1)
    skillTypeOpsMock.serialize(skill2) returns Option(serializedSkill2)

    testMessage.write(bufferMock)

    bufferMock.writeUUID(testUuid) wasCalled once
    bufferMock.writeBoolean(false) wasCalled once
    bufferMock.writeInt(*) wasCalled thrice
    bufferMock.writeUtf(*, *) wasCalled twice

    loggerMock.debug(*) wasCalled once
  }

  "SyncSkillsMessage.handle" should "resyncs clientside data" in {
    val netHandlerMock = mock[NetHandler]
    clientStateMock.getNetHandler returns netHandlerMock
    testMessage.handle(packetContextMock)

    netHandlerMock.onSyncPlayer(skills, force = false) wasCalled once
  }

  "SyncSkillsMessageFactory.receive" should "throw an error if sending without a message type" in {
    val serializedSkill1 = "testone"
    val serializedSkill2 = "testtwoo"

    bufferMock.readUUID() returns testUuid
    bufferMock.readBoolean() returns true
    bufferMock.readInt() returns skills.length andThen serializedSkill1.length andThen serializedSkill2.length
    bufferMock.readUtf(serializedSkill1.length) returns serializedSkill1
    bufferMock.readUtf(serializedSkill2.length) returns serializedSkill2

    skillTypeOpsMock.deserialize(serializedSkill1) returns Option(skill1)
    skillTypeOpsMock.deserialize(serializedSkill2) returns Option(skill2)

    testFactory.receive(bufferMock) shouldBe null
  }

  it should "returns a new message if there is a message type" in {
    val serializedSkill1 = "testone"
    val serializedSkill2 = "testtwoo"

    bufferMock.readUUID() returns testUuid
    bufferMock.readBoolean() returns true
    bufferMock.readInt() returns skills.length andThen serializedSkill1.length andThen serializedSkill2.length
    bufferMock.readUtf(serializedSkill1.length) returns serializedSkill1
    bufferMock.readUtf(serializedSkill2.length) returns serializedSkill2

    skillTypeOpsMock.deserialize(serializedSkill1) returns Option(skill1)
    skillTypeOpsMock.deserialize(serializedSkill2) returns Option(skill2)

    testFactory.setMessageType(messageTypeMock.value)

    val response = testFactory.receive(bufferMock)

    loggerMock.error(*) wasNever called

    response.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.send" should "throw an error if sending without a message type" in {
    playerMock.uuid returns testUuid

    testFactory.send(playerMock, skills, force = true) shouldBe None
  }

  it should "returns a new message if there is a message type" in {
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid

    testFactory.setMessageType(messageTypeMock.value)

    val response = testFactory.send(playerMock, skills, force = true)

    loggerMock.error(*) wasNever called

    response.value.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.apply" should "creates a valid class" in {
    SyncSkillsMessageFactory().isInstanceOf[SyncSkillsMessageFactory] should be(true)
  }
}
