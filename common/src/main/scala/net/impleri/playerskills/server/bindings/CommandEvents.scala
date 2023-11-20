package net.impleri.playerskills.server.bindings

import dev.architectury.event.events.common.CommandRegistrationEvent
import net.impleri.playerskills.server.commands.PlayerSkillsCommands

case class CommandEvents() {
  def registerEvents(commands: PlayerSkillsCommands): Unit = {
    CommandRegistrationEvent
      .EVENT
      .register((d, _, _) => commands.register(d))
  }
}
