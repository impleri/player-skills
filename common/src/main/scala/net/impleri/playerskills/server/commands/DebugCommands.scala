package net.impleri.playerskills.server.commands

import net.impleri.playerskills.utils.{LoggerType, PlayerSkillsLogger}
import net.impleri.slab.chat.Message
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandPermission
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString

trait DebugCommands {
  protected def registerDebugCommands[T <: CommandSegment.Any](
    builder: T,
  ): T =
    builder
      .option(
        CommandString("debug")
          .requires(CommandPermission.MOD)
          .option(
            CommandString("skills").executes(
              CommandAction(handler("Skills", LoggerType.SKILLS)).message(),
            ),
          )
          .option(
            CommandString("blocks").executes(
              CommandAction(handler("Block Restrictions", LoggerType.BLOCKS))
                .message(),
            ),
          )
          .option(
            CommandString("fluids").executes(
              CommandAction(handler("Fluid Restrictions", LoggerType.FLUIDS))
                .message(),
            ),
          )
          .option(
            CommandString("items").executes(
              CommandAction(handler("Item Restrictions", LoggerType.ITEMS)).message(),
            ),
          )
          .option(
            CommandString("recipes").executes(
              CommandAction(handler("Recipe Restrictions", LoggerType.RECIPES)).message(),
            ),
          )
          .option(
            CommandString("mobs").executes(
              CommandAction(handler("Mob Restrictions", LoggerType.MOBS)).message(),
            ),
          ),
      )
      .asInstanceOf[T]

  private[commands] def handler(
    modLabel: String,
    loggerType: LoggerType,
  ): CommandAction.Callback = { _ =>
    toggleDebug(modLabel, loggerType)
  }

  protected[commands] def toggleDebug(
    modLabel: String,
    loggerType: LoggerType,
  ): Either[Message[_], Message[_]] =
    if (PlayerSkillsLogger.toggleDebug(Option(loggerType))) {
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
