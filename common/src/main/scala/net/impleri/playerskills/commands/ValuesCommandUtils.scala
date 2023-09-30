package net.impleri.playerskills.commands

import com.mojang.brigadier.Command
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ValuesCommandUtils {
  protected def listValues(source: CommandSourceStack, message: Component, values: List[String]): Int = {
    source.sendSuccess(message, false)
    values.foreach(t => source.sendSystemMessage(Component.literal(t)))

    Command.SINGLE_SUCCESS
  }
}
