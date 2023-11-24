package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.Event
import net.impleri.playerskills.server.commands.PlayerSkillsCommands

case class CommandEvents(event: Event[CommandRegistrationEvent] = CommandRegistrationEvent.EVENT) {
  def registerEvents(commands: PlayerSkillsCommands): Unit = {
    event.register((d, _, _) => commands.register(d))
  }
}
