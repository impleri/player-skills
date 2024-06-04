package net.impleri.playerskills.client

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.network.ResyncSkillsMessageFactory
import net.impleri.slab.client.Client
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.minecraft.client.player.LocalPlayer

import java.util.UUID


class NetHandlerSpec extends BaseSpec {
  private val clientMock = mock[Client]
  private val clientSkillsMock = mock[ClientSkillsRegistry]
  private val loggerMock = mock[Logger]
  private val messageFactoryMock = mock[ResyncSkillsMessageFactory]

  private val givenUuid = UUID.randomUUID()
  private val playerMock = mock[Player[LocalPlayer]]
  playerMock.uuid returns givenUuid
  clientMock.getPlayer returns Option(playerMock)

  private val testUnit = NetHandler(clientMock, clientSkillsMock, messageFactoryMock, loggerMock)

  "NetHandler.resyncPlayer" should "send a request to the server" in {
    messageFactoryMock.send(playerMock) returns None

    testUnit.resyncPlayer(playerMock)

    loggerMock.debug(*) wasCalled once
    messageFactoryMock.send(playerMock) wasCalled once
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
