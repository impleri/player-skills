package net.impleri.playerskills.client

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.slab.client.Player
import net.impleri.slab.logging.Logger

import java.util.UUID

class NetHandlerSpec extends BaseSpec {
  private val clientSkillsMock = mock[ClientSkillsRegistry]
  private val loggerMock = mock[Logger]
  private val messageFactoryMock = mock[ResyncSkillsMessageFactory]

  private val givenUuid = UUID.randomUUID()
  private val playerMock = mock[Player]
  playerMock.uuid returns givenUuid

  private val testUnit = NetHandler(clientSkillsMock, messageFactoryMock, loggerMock)

  "NetHandler.resyncPlayer" should "send a request to the server" in {
    messageFactoryMock.send() returns None

    testUnit.resyncPlayer(playerMock)

    loggerMock.debug(*) wasCalled once
    messageFactoryMock.send() wasCalled once
  }

  "NetHandler.onSyncPlayer" should "update stored skills" in {
    val forced = false

    val skills = mock[List[Skill[_]]]
    skills.map[String](*) returns List("string", "two")

    testUnit.onSyncPlayer(skills, forced)

    loggerMock.info(*) wasCalled once
    clientSkillsMock.update(skills, forced) wasCalled once
  }

  "NetHandler.apply" should "return a usable instance" in {
    val unit = NetHandler(messageFactory = messageFactoryMock)

    unit.isInstanceOf[NetHandler] should be(true)
  }
}
