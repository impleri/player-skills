package net.impleri.playerskills.events.handlers

import com.mojang.brigadier.CommandDispatcher
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.impleri.playerskills.commands.PlayerSkillsCommands
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

internal class CommandEvents {
  fun registerEvents() {
    CommandRegistrationEvent.EVENT.register(
      CommandRegistrationEvent { dispatcher: CommandDispatcher<CommandSourceStack>, _: CommandBuildContext, _: Commands.CommandSelection ->
        PlayerSkillsCommands.register(dispatcher)
      },
    )
  }
}
