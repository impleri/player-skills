package net.impleri.playerskills.server

import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.network.SyncSkillsMessage
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.skills.PlayerRegistryState
import net.impleri.playerskills.skills.SkillRegistry
import net.impleri.slab.entity.{Player => MinecraftPlayer}
import net.impleri.slab.logging.Logger
import net.impleri.slab.network.ClientboundMessage
import net.impleri.slab.network.Network
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ReloadListeners
import net.impleri.slab.resources.ResourceManager
import net.impleri.slab.server.Server
import net.minecraft.server.level.ServerPlayer

import java.util.UUID

private class ServerStateContainerSpec extends BaseSpec {
  private val globalStateMock = mock[StateContainer]
  private val loggerMock = mock[Logger]
  private val serverMock = mock[Server]
  private val playerRegistryMock = mock[PlayerRegistry]
  private val eventHandlerMock = mock[EventHandler]
  private val teamMock = mock[Team]
  private val reloadListenersMock = mock[ReloadListeners]
  private val skillRegistryMock = mock[SkillRegistry]
  private val skillTypeOpsMock = mock[SkillTypeOps]
  private val networkMock = mock[Network]
  private val registryMock = mock[Registry.ITEM]

  lazy private val testUnit = ServerStateContainer(
    globalStateMock,
    playerRegistryMock,
    eventHandlerMock,
    reloadListenersMock,
    StubTeam(),
    None,
    registryMock,
    loggerMock
  )

  lazy private val testUnitWithServer = ServerStateContainer(
    globalStateMock,
    playerRegistryMock,
    eventHandlerMock,
    reloadListenersMock,
    StubTeam(),
    Option(serverMock),
    registryMock,
    loggerMock
  )

  "ServerStateContainer init" should "logs the startup" in {
    globalStateMock.NETWORK returns networkMock
    testUnit
    loggerMock.info(*) wasCalled once
  }

  "ServerStateContainer.setTeam" should "change the team instance" in {
    globalStateMock.NETWORK returns networkMock

    testUnit.TEAM.isInstanceOf[StubTeam] should be(true)
    testUnit.setTeam(teamMock)
    testUnit.TEAM.isInstanceOf[StubTeam] should be(false)
  }

  "ServerStateContainer.onServerChange" should "clear the server if None" in {
    val playerRegistryState = PlayerRegistryState.empty

    globalStateMock.NETWORK returns networkMock
    globalStateMock.SKILL_TYPE_OPS returns skillTypeOpsMock

    playerRegistryMock.close() returns List.empty

    testUnitWithServer.SERVER.value should be(serverMock)

    testUnitWithServer.onServerChange()

    testUnitWithServer.SERVER should be(None)

    globalStateMock.SKILL_TYPE_OPS wasCalled sixTimes
  }

  it should "change the team instance" in {
    val playerRegistryState = PlayerRegistryState.empty

    globalStateMock.NETWORK returns networkMock
    globalStateMock.SKILL_TYPE_OPS returns skillTypeOpsMock

    playerRegistryMock.close() returns List.empty

    serverMock.getPlayers returns Seq.empty

    testUnit.SERVER should be(None)

    testUnit.onServerChange(Option(serverMock))

    testUnit.SERVER.value should be(serverMock)

    globalStateMock.SKILL_TYPE_OPS wasCalled sixTimes
  }

  "ServerStateContainer.onReload" should "resync all players" in {
    val givenUuid = UUID.randomUUID()

    val playerMock = mock[MinecraftPlayer]
    playerMock.uuid returns givenUuid
    val currentUsers = List(givenUuid)

    val messageTypeMock = mock[MessageType]
    networkMock
      .registerMessageToClient[SyncSkillsMessage](*, *) returns messageTypeMock
    globalStateMock.NETWORK returns networkMock

    playerRegistryMock.close() returns currentUsers
    serverMock.getPlayers returns List(playerMock)

    testUnitWithServer.SERVER.value should be(serverMock)
    val skills = List.empty
    playerRegistryMock.get(givenUuid) returns skills

    testUnitWithServer.onReload(mock[Option[ResourceManager]])

    playerRegistryMock.open(currentUsers) wasCalled once

    playerMock.sendMessage(any[ClientboundMessage]) wasCalled once
  }
}
