package net.impleri.playerskills.server

import dev.architectury.networking.simple.BaseS2CMessage
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.network.SyncSkillsMessage
import net.impleri.playerskills.network.SyncSkillsMessageFactory
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.level.ServerPlayer

class NetHandlerSpec extends BaseSpec {
  private val playerOpsMock = mock[Player]
  private val messageFactoryMock = mock[SyncSkillsMessageFactory]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUnit = NetHandler(playerOpsMock, messageFactoryMock, loggerMock)

  private val playerMock = mock[MinecraftPlayer[ServerPlayer]]

  "NetHandler.syncPlayer" should "send all of the player's current skills" in {
    val skills = List.empty
    playerOpsMock.get(playerMock) returns skills

    testUnit.syncPlayer(playerMock)

    loggerMock.debugP(*)(*) wasCalled once
    playerMock.sendMessage(any[SyncSkillsMessage]) wasCalled once
  }

  it should "update a player after a SkillChangedEvent" in {
    val event = SkillChangedEvent(playerMock, None, None)
    playerMock.isServer returns true

    val skills = List.empty
    playerOpsMock.get(playerMock) returns skills

    testUnit.syncPlayer(event)

    loggerMock.debugP(*)(*) wasCalled once
    playerMock.sendMessage(any[SyncSkillsMessage]) wasCalled once
  }

  it should "log a warning if called clientside" in {
    val event = SkillChangedEvent(playerMock, None, None)
    playerMock.isServer returns false

    testUnit.syncPlayer(event)

    loggerMock.warn(*) wasCalled once
    playerOpsMock.get(playerMock) wasNever called
    loggerMock.debugP(*)(*) wasNever called
    playerMock.sendMessage(any[BaseS2CMessage]) wasNever called
  }

  "NetHandler.apply" should "return a workable instance" in {
    val result = NetHandler(messageFactory = messageFactoryMock)

    result.isInstanceOf[NetHandler] should be(true)
  }
}
