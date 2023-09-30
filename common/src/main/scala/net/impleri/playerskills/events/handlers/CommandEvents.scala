package net.impleri.playerskills.events.handlers

import dev.architectury.event.events.common.CommandRegistrationEvent
import net.impleri.playerskills.commands.PlayerSkillsCommands

case class CommandEvents() {
  def registerEvents(): Unit = CommandRegistrationEvent.EVENT.register((d, _, _) => PlayerSkillsCommands.register(d))
}
