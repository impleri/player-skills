package net.impleri.playerskills.network

import dev.architectury.networking.simple.MessageType
import dev.architectury.networking.NetworkManager
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientSkillsRegistry
import net.impleri.playerskills.facades.architectury.Network
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer

import java.util.UUID

class SyncSkillsMessageSpec extends BaseSpec {
  private val messageTypeMock = mock[MessageType]

  private val skillTypeOpsMock = mock[SkillTypeOps]
  private val clientRegistryMock = mock[ClientSkillsRegistry]
  private val networkMock = mock[Network]
  private val serverStateMock = mock[ServerStateContainer]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUuid = UUID.randomUUID()
  private val skill1 = mock[Skill[Boolean]]
  private val skill2 = mock[Skill[String]]
  private val skills = List(skill1, skill2)

  private val testMessage = SyncSkillsMessage(testUuid,
    skills,
    false,
    skillTypeOpsMock,
    Option(clientRegistryMock),
    messageTypeMock,
    loggerMock,
  )
  private val testFactory = SyncSkillsMessageFactory(skillTypeOpsMock,
    Option(clientRegistryMock),
    networkMock,
    loggerMock,
  )

  private val packetContextMock = mock[NetworkManager.PacketContext]
  private val playerMock = mock[Player[ServerPlayer]]
  private val bufferMock = mock[FriendlyByteBuf]

  "SyncSkillsMessage.getType" should "return the messageType" in {
    testMessage.getType should be(messageTypeMock)
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
    testMessage.handle(packetContextMock)

    clientRegistryMock.syncFromServer(skills, false) wasCalled once
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

    assertThrows[Throwable] {
      testFactory.receive(bufferMock)
    }

    loggerMock.debug(*) wasCalled once

    loggerMock.error(*) wasCalled once
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

    testFactory.setMessageType(messageTypeMock)

    val response = testFactory.receive(bufferMock)

    loggerMock.error(*) wasNever called

    response.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.send" should "throw an error if sending without a message type" in {
    playerMock.uuid returns testUuid

    assertThrows[Throwable] {
      testFactory.send(playerMock, skills, true)
    }

    loggerMock.error(*) wasCalled once
  }

  it should "returns a new message if there is a message type" in {
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid

    testFactory.setMessageType(messageTypeMock)

    val response = testFactory.send(playerMock, skills, true)

    loggerMock.error(*) wasNever called

    response.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.apply" should "creates a valid class" in {
    SyncSkillsMessageFactory().isInstanceOf[SyncSkillsMessageFactory] should be(true)
  }
}
