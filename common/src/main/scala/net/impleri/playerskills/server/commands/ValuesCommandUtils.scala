package net.impleri.playerskills.server.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ValuesCommandUtils {
  private def listValues(source: CommandSourceStack, message: Component, values: List[String]): Int = {
    source.sendSuccess(message, false)
    values.foreach(t => source.sendSystemMessage(Component.literal(t)))

    Command.SINGLE_SUCCESS
  }

  protected def withListValuesContext(f: CommandContext[CommandSourceStack] => (Component, List[String])): Command[CommandSourceStack] = {
    (context: CommandContext[CommandSourceStack]) => {
      val (message, values) = f(context)

      listValues(context.getSource, message, values)
    }
  }

  protected[commands] def withListValuesSource(f: CommandSourceStack => (Component, List[String])): Command[CommandSourceStack] = {
    withListValuesContext(context => f(context.getSource))
  }

  protected[commands] def withListValues(f: () => (Component, List[String])): Command[CommandSourceStack] = {
    withListValuesContext(_ => f())
  }
}
