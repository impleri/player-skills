package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.server.commands.PlayerSkillsCommands
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.slab.commands.backport.FillBiomeCommand
import net.impleri.slab.events.BlockEvents
import net.impleri.slab.events.CommandEvents
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.events.ServerLifecycleEvents
import net.impleri.slab.events.TickEvents
import net.impleri.slab.server.Server

class ServerEventBindingsSpec extends BaseSpec {
  private val mockPlayerRegistry = mock[PlayerRegistry]
  private val mockStateContainer = mock[StateContainer]
  private val mockServerStateContainer = mock[ServerStateContainer]
  private val mockOps = mock[ItemRestrictionOps]
  private val mockOnChange = mock[Option[Server] => Unit]
  private val mockCommands = mock[PlayerSkillsCommands]
  private val mockNetHandler = mock[NetHandler]
  private val mockFillBiome = mock[FillBiomeCommand]
  private val mockCommonLifecycle = mock[CommonLifecycleEvents]
  private val mockServerLifecycle = mock[ServerLifecycleEvents]
  private val mockCommand = mock[CommandEvents]
  private val mockEntity = mock[EntityEvents]
  private val mockBlock = mock[BlockEvents]
  private val mockPlayer = mock[PlayerEvents]
  private val mockTick = mock[TickEvents]

  mockStateContainer.ITEM_RESTRICTIONS returns mockOps
  mockServerStateContainer.PLAYERS returns mockPlayerRegistry

  private val testUnit = ServerEventBindings(
    mockStateContainer,
    mockServerStateContainer,
    mockOnChange,
    () => mockCommands,
    mockNetHandler,
    mockFillBiome,
    mockCommonLifecycle,
    mockServerLifecycle,
    mockCommand,
    mockEntity,
    mockBlock,
    mockPlayer,
    mockTick,
  )

  "LifecycleEvents.registerEvents" should "bind events" in {
    testUnit.registerEvents()

    mockCommonLifecycle.onSetup(*) wasCalled once
    mockServerLifecycle.beforeServerStart(*) wasCalled once
    mockServerLifecycle.beforeServerStop(*) wasCalled twice

    mockPlayer.onJoin(*) wasCalled once
    mockPlayer.onQuit(*) wasCalled once

    mockBlock.onBreak(*) wasCalled once

    mockEntity.onHurt(*) wasCalled once

    mockTick.onPlayerEnd(*, *, *) wasCalled once

    mockCommand.register(mockCommands) wasCalled once
    mockCommand.register(mockFillBiome) wasCalled once

  }
}
