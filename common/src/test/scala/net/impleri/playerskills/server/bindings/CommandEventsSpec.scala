package net.impleri.playerskills.server.bindings

import dev.architectury.event.Event
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.server.commands.PlayerSkillsCommands

class CommandEventsSpec extends BaseSpec {
  "CommandEvents.registerEvents" should "register commands" in {
    val mockEvent = mock[Event[CommandRegistrationEvent]]
    val commandsMock = mock[PlayerSkillsCommands]

    CommandEvents(mockEvent).registerEvents(commandsMock)

    mockEvent.register(*) wasCalled once
  }
}
