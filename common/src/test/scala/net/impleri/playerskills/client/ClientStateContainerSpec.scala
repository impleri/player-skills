package net.impleri.playerskills.client

import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.network.ResyncSkillsMessage
import net.impleri.playerskills.network.SyncSkillsMessage
import net.impleri.slab.client.Client
import net.impleri.slab.network.Network

private class ClientStateContainerSpec extends BaseSpec {
  private val globalStateMock = mock[StateContainer]
  private val clientMock = mock[Client]
  private val eventHandlerMock = mock[EventHandler]

  lazy private val testUnit = ClientStateContainer(globalStateMock, eventHandlerMock, clientMock)

  "ClientStateContainer.getNetHandler" should "create a C2S network handler" in {
    val messageTypeMock = mock[MessageType]
    val networkMock = mock[Network]
    networkMock.registerMessageToClient[SyncSkillsMessage](*, *) returns messageTypeMock
    networkMock.registerMessageToServer[ResyncSkillsMessage](*, *) returns messageTypeMock
    globalStateMock.NETWORK returns networkMock

    testUnit.getNetHandler.isInstanceOf[NetHandler] should be(true)
  }

  "ClientStateContainer.apply" should "create a usable instance" in {
    val result = ClientStateContainer()

    result.isInstanceOf[ClientStateContainer] should be(true)
  }
}
