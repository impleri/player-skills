package net.impleri.playerskills.server.commands

import net.impleri.slab.chat.Message
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandPermission
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.logging.Logger

trait DebugCommands {
  protected def logger: Logger

  protected def itemLogger: Logger

  protected def blockLogger: Logger

  protected def fluidLogger: Logger

  protected def mobLogger: Logger

  protected def registerDebugCommands[T <: CommandSegment.Any](
    builder: T,
  ): T =
    builder
      .option(
        CommandString("debug")
          .requires(CommandPermission.MOD)
          .option(
            CommandString("skills").executes(
              CommandAction(handler("Skills", logger)).message(),
            ),
          )
          .option(
            CommandString("blocks").executes(
              CommandAction(handler("Block Restrictions", blockLogger))
                .message(),
            ),
          )
          .option(
            CommandString("fluids").executes(
              CommandAction(handler("Fluid Restrictions", fluidLogger))
                .message(),
            ),
          )
          .option(
            CommandString("items").executes(
              CommandAction(handler("Item Restrictions", itemLogger)).message(),
            ),
          )
          .option(
            CommandString("mobs").executes(
              CommandAction(handler("Mob Restrictions", mobLogger)).message(),
            ),
          ),
      )
      .asInstanceOf[T]

  private[commands] def handler(
    modLabel: String,
    logInstance: Logger,
  ): CommandAction.Callback = { _ =>
    toggleDebug(modLabel, logInstance)
  }

  protected[commands] def toggleDebug(
    modLabel: String,
    logInstance: Logger,
  ): Either[Message[_], Message[_]] =
    if (logInstance.toggleDebug()) {
      Right(
        TranslatableText("commands.playerskills.debug_enabled", modLabel)
          .red()
          .bold(),
      )
    } else {
      Right(
        TranslatableText("commands.playerskills.debug_disabled", modLabel)
          .green()
          .italic(),
      )
    }
}
