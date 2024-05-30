package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.impleri.slab.commands.BaseCommand

case class CommandEvents(
  private val registerEvent: Event[CommandRegistrationEvent] = CommandRegistrationEvent.EVENT,
) {
  def register(command: BaseCommand): Unit = {
    registerEvent.register { (dispatch, _, _) =>
      command.register(dispatch)
    }
  }
}
