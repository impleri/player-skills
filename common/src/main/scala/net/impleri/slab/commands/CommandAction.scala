package net.impleri.slab.commands

import com.mojang.brigadier.{Command => McCommand}
import net.impleri.slab.chat.Message
import net.impleri.slab.entity.Player

case class CommandAction(
  f: CommandAction.Callback,
  fallbackMessage: Option[Message.Any] = None,
  responseCode: Int = McCommand.SINGLE_SUCCESS,
  responseType: ResponseType.Value = ResponseType.NONE,
) extends CommandAction.Executor {
  private final val COMMAND_FAILURE = 0

  private def sendMessage(
    context: Command.Context,
  )(message: Message.Any): Int = {
    responseType match {
      case ResponseType.MESSAGE       => message.sendSuccess(context)
      case ResponseType.MESSAGE_ADMIN => message.sendSuccessWithAdmins(context)
      case _                          =>
    }

    responseCode
  }

  private def sendFailure(
    context: Command.Context,
  )(message: Message.Any): Int = {
    responseType match {
      case ResponseType.MESSAGE | ResponseType.MESSAGE_ADMIN =>
        message.sendFailure(context)
      case _ =>
    }

    COMMAND_FAILURE
  }

  def run(context: Command.Context): Int =
    f(context).fold(sendFailure(context), sendMessage(context))

  def silent(): CommandAction = copy(responseType = ResponseType.NONE)

  def message(includeAdmins: Boolean = false): CommandAction =
    copy(responseType =
      if (includeAdmins) ResponseType.MESSAGE_ADMIN else ResponseType.MESSAGE,
    )
}

object CommandAction {
  private type Executor = McCommand[Command.Source]
  type Callback = Command.Context => Either[Message.Any, Message.Any]

  def getCurrentPlayer(context: Command.Context): Option[Player] = {
    Option(context)
      .map(_.getSource)
      .map(_.getPlayer)
      .map(Player(_))
  }

  def getPlayer(
    context: Command.Context,
    skipArgument: Boolean = false,
  ): Option[Player] = {
    val p = if (skipArgument) None else PlayerArgument.getValue(context)

    p.orElse(getCurrentPlayer(context))
  }
}

object ResponseType extends Enumeration {
  val NONE, MESSAGE, MESSAGE_ADMIN = Value
}
