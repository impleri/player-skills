package net.impleri.playerskills.server.bindings

import net.impleri.playerskills.server.bindings.block.OnBreak
import net.impleri.playerskills.server.bindings.entity.OnHurt
import net.impleri.playerskills.server.bindings.lifecycle.BeforeServerStops
import net.impleri.playerskills.server.commands.PlayerSkillsCommands
import net.impleri.playerskills.server.NetHandler
import net.impleri.playerskills.server.bindings.player.OnJoin
import net.impleri.playerskills.server.bindings.player.OnPlayerTick
import net.impleri.playerskills.server.bindings.player.OnQuit
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.server.bindings.lifecycle.OnSetup
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.commands.backport.FillBiomeCommand
import net.impleri.slab.events.BlockEvents
import net.impleri.slab.events.CommandEvents
import net.impleri.slab.events.CommonLifecycleEvents
import net.impleri.slab.events.EntityEvents
import net.impleri.slab.events.PlayerEvents
import net.impleri.slab.events.ServerLifecycleEvents
import net.impleri.slab.events.TickEvents
import net.impleri.slab.server.Server

case class ServerEventBindings(
  globalState: StateContainer,
  serverState: ServerStateContainer,
  onServerChange: Option[Server] => Unit = _ => {},
  getCommand: () => PlayerSkillsCommands,
  netHandler: NetHandler = NetHandler(),
  fillBiomeCommand: FillBiomeCommand = FillBiomeCommand(Option(PlayerSkillsLogger.ITEMS)),
  commonLifecycle: CommonLifecycleEvents = CommonLifecycleEvents(),
  serverLifecycle: ServerLifecycleEvents = ServerLifecycleEvents(),
  commands: CommandEvents = CommandEvents(),
  entities: EntityEvents = EntityEvents(),
  blocks: BlockEvents = BlockEvents(),
  players: PlayerEvents = PlayerEvents(),
  ticks: TickEvents = TickEvents(),
) {
  private[server] def registerEvents(): Unit = {
    OnSetup(globalState, serverState, commonLifecycle)

    serverLifecycle.beforeServerStart(onServerChange)
    serverLifecycle.beforeServerStop(onServerChange)
    BeforeServerStops(() => serverState.PLAYERS, serverLifecycle)

    OnJoin(() => serverState.PLAYERS, netHandler, players)
    OnQuit(() => serverState.PLAYERS, netHandler, players)

    OnBreak(globalState.ITEM_RESTRICTIONS, blocks)

    OnHurt(globalState.ITEM_RESTRICTIONS, entities)

    OnPlayerTick(globalState.ITEM_RESTRICTIONS, ticks)

    commands.register(getCommand())
    commands.register(fillBiomeCommand)
  }
}
