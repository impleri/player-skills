package net.impleri.playerskills.server

import dev.architectury.networking.simple.BaseS2CMessage
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.StubTeam
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.server.skills.PlayerRegistryState
import net.impleri.playerskills.skills.SkillRegistry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.resources.ResourceManager

import java.util.UUID

private class ServerStateContainerSpec extends BaseSpec {
  private val globalStateMock = mock[StateContainer]
  private val loggerMock = mock[PlayerSkillsLogger]
  private val serverMock = mock[MinecraftServer]
  private val registryMock = mock[PlayerRegistry]
  private val teamMock = mock[Team]
  private val skillRegistryMock = mock[SkillRegistry]
  private val skillTypeOpsMock = mock[SkillTypeOps]

  private val testUnit = ServerStateContainer(registryMock, globalStateMock, StubTeam(), None, loggerMock)

  "ServerStateContainer.init" should "logs the startup" in {
    testUnit.init()
    loggerMock.info(*) wasCalled once
  }

  "ServerStateContainer.setTeam" should "change the team instance" in {
    testUnit.TEAM.isInstanceOf[StubTeam] should be(true)
    testUnit.setTeam(teamMock)
    testUnit.TEAM.isInstanceOf[StubTeam] should be(false)
  }

  "ServerStateContainer.onServerChange" should "clear the server if None" in {
    val playerRegistryState = PlayerRegistryState.empty
    val testUnit = ServerStateContainer(registryMock, globalStateMock, StubTeam(), Option(serverMock), loggerMock)
    globalStateMock.getSkillTypeOps returns skillTypeOpsMock
    globalStateMock.SKILLS returns skillRegistryMock
    registryMock.getState returns playerRegistryState

    testUnit.SERVER.value should be(serverMock)

    testUnit.onServerChange()

    testUnit.SERVER should be(None)

    globalStateMock.getSkillTypeOps wasCalled once
    globalStateMock.SKILLS wasCalled once
    registryMock.getState wasCalled once
  }

  it should "change the team instance" in {
    val playerRegistryState = PlayerRegistryState.empty
    globalStateMock.getSkillTypeOps returns skillTypeOpsMock
    globalStateMock.SKILLS returns skillRegistryMock
    registryMock.getState returns playerRegistryState

    testUnit.SERVER should be(None)

    testUnit.onServerChange(Option(serverMock))

    testUnit.SERVER.value should be(serverMock)

    globalStateMock.getSkillTypeOps wasCalled once
    globalStateMock.SKILLS wasCalled once
    registryMock.getState wasCalled once
  }

  "ServerStateContainer.onReload" should "resync all players" in {
    val testUnit = ServerStateContainer(registryMock, globalStateMock, StubTeam(), Option(serverMock), loggerMock)
    val givenUuid = UUID.randomUUID()

    val playerMock = mock[MinecraftPlayer[ServerPlayer]]
    playerMock.uuid returns givenUuid
    val currentUsers = List(givenUuid)
    registryMock.close() returns currentUsers
    serverMock.getPlayers returns List(playerMock)

    testUnit.SERVER.value should be(serverMock)
    val skills = List.empty
    registryMock.get(givenUuid) returns skills

    testUnit.onReload(mock[ResourceManager])

    registryMock.open(currentUsers) wasCalled once

    playerMock.sendMessage(any[BaseS2CMessage]) wasCalled once
  }

  "ServerStateContainer.getPlayerOps" should "return a working instance" in {
    val skillOpsMock = mock[SkillOps]

    globalStateMock.getSkillOps returns skillOpsMock
    globalStateMock.getSkillTypeOps returns skillTypeOpsMock

    testUnit.getPlayerOps.isInstanceOf[Player] should be(true)
  }

  "ServerStateContainer.getTeamOps" should "return a working instance" in {
    val skillOpsMock = mock[SkillOps]

    globalStateMock.getSkillOps returns skillOpsMock
    globalStateMock.getSkillTypeOps returns skillTypeOpsMock

    testUnit.getTeamOps.isInstanceOf[TeamOps] should be(true)
  }

  "ServerStateContainer.apply" should "create a usable instance" in {
    val result = ServerStateContainer()

    result.isInstanceOf[ServerStateContainer] should be(true)
  }
}
