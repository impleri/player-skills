package net.impleri.playerskills.network

import dev.architectury.networking.simple.{MessageType => ArchMessageType}
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.client.ClientStateContainer
import net.impleri.playerskills.client.NetHandler
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.{FriendlyBuffer, MessageType}

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

  private val testMessage = SyncSkillsMessage(
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

  private val playerMock = mock[Player]
  private val bufferMock = mock[FriendlyBuffer]

  private val underlyingMessageType = mock[ArchMessageType]
  messageTypeMock.value returns underlyingMessageType

  "SyncSkillsMessage.getType" should "return the messageType" in {
    testMessage.getType should be(underlyingMessageType)
  }

  "SyncSkillsMessage.write" should "create the right buffer" in {
    val serializedSkill1 = "testone"
    val serializedSkill2 = "testtwoo"

    bufferMock.writeBoolean(false) returns bufferMock

    skillTypeOpsMock.serialize(skill1) returns Option(serializedSkill1)
    skillTypeOpsMock.serialize(skill2) returns Option(serializedSkill2)

    testMessage.onSend(bufferMock)

    bufferMock.writeBoolean(false) wasCalled once
    bufferMock.writeStrings(Seq(serializedSkill1, serializedSkill2)) wasCalled once
  }

  "SyncSkillsMessage.onReceive" should "resyncs clientside data" in {
    val netHandlerMock = mock[NetHandler]
    clientStateMock.getNetHandler returns netHandlerMock

    testMessage.onReceive(Option(playerMock))

    netHandlerMock.onSyncPlayer(skills, force = false) wasCalled once
  }

  "SyncSkillsMessageFactory.parse" should "returns a new message if there is a message type" in {
    val serializedSkill1 = "testone"
    val serializedSkill2 = "testtwoo"

    bufferMock.readBoolean() returns Option(true)
    bufferMock.readStrings() returns Seq(serializedSkill1, serializedSkill2)

    skillTypeOpsMock.deserialize(serializedSkill1) returns Option(skill1)
    skillTypeOpsMock.deserialize(serializedSkill2) returns Option(skill2)

    val response = testFactory.parse(bufferMock, messageTypeMock)

    loggerMock.error(*) wasNever called

    response.value.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.send" should "throw an error if sending without a message type" in {
    playerMock.uuid returns testUuid

    testFactory.send(skills, force = true) shouldBe None
  }

  it should "returns a new message if there is a message type" in {
    val givenUuid = UUID.randomUUID()

    playerMock.uuid returns givenUuid

    testFactory.setMessageType(messageTypeMock.value)

    val response = testFactory.send(skills, force = true)

    loggerMock.error(*) wasNever called

    response.value.isInstanceOf[SyncSkillsMessage] should be(true)
  }

  "SyncSkillsMessageFactory.apply" should "creates a valid class" in {
    SyncSkillsMessageFactory().isInstanceOf[SyncSkillsMessageFactory] should be(true)
  }
}
