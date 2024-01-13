package net.impleri.playerskills.client

import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.facades.architectury.Network
import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.network.ResyncSkillsMessage
import net.impleri.playerskills.network.SyncSkillsMessage

private class ClientStateContainerSpec extends BaseSpec {
  private val globalStateMock = mock[StateContainer]
  private val clientMock = mock[Client]
  private val eventHandlerMock = mock[EventHandler]

  lazy private val testUnit = ClientStateContainer(globalStateMock, eventHandlerMock, clientMock)

  "ClientStateContainer.getNetHandler" should "create a C2S network handler" in {
    val messageTypeMock = mock[MessageType]
    val networkMock = mock[Network]
    networkMock.registerClientboundMessage[SyncSkillsMessage](*, *) returns messageTypeMock
    networkMock.registerServerboundMessage[ResyncSkillsMessage](*, *) returns messageTypeMock
    globalStateMock.NETWORK returns networkMock

    testUnit.getNetHandler.isInstanceOf[NetHandler] should be(true)
  }

  "ClientStateContainer.apply" should "create a usable instance" in {
    val result = ClientStateContainer()

    result.isInstanceOf[ClientStateContainer] should be(true)
  }
}
