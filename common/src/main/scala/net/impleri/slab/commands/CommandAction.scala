package net.impleri.slab.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import net.impleri.slab.chat.Message
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.commands.arguments.{ResourceLocationArgument => McResourceLocationArgument}

import scala.util.Try

case class CommandAction[T](
  f: CommandCallback,
  fallbackMessage: Option[Message[_]] = None,
  responseCode: Int = Command.SINGLE_SUCCESS,
  responseType: ResponseType.Value = ResponseType.NONE,
)
  extends CommandExecution {
  private final val COMMAND_FAILURE = 0

  private def sendMessage(context: CommandContext)(message: Message[_]): Int = {
    responseType match {
      case ResponseType.MESSAGE => message.sendSuccess(context)
      case ResponseType.MESSAGE_ADMIN => message.sendSuccessWithAdmins(context)
      case _ =>
    }

    responseCode
  }

  private def sendFailure(context: CommandContext)(message: Message[_]): Int = {
    responseType match {
      case ResponseType.MESSAGE | ResponseType.MESSAGE_ADMIN => message.sendFailure(context)
      case _ =>
    }

    COMMAND_FAILURE
  }

  def run(context: CommandContext): Int = f(context).fold(sendFailure(context), sendMessage(context))

  def silent(): CommandAction[T] = copy(responseType = ResponseType.NONE)

  def message(includeAdmins: Boolean = false): CommandAction[T] = {
    copy(responseType = if (includeAdmins) ResponseType.MESSAGE_ADMIN else ResponseType.MESSAGE)
  }
}

object CommandAction {
  def getCurrentPlayer(context: CommandContext): Option[Player.Server] = {
    Option(context)
      .map(_.getSource)
      .map(_.getPlayer)
      .map(Player(_))
  }

  def getPlayerArgument(context: CommandContext): Option[Player.Server] = {
    Option(context)
      .map(_.getSource)
      .map(s => Try(s.getPlayerOrException))
      .flatMap(_.toOption)
      .map(Player(_))
  }

  def getPlayer(context: CommandContext, skipArgument: Boolean = false): Option[Player.Server] = {
    val p = if (skipArgument) None else getPlayerArgument(context)

    p.orElse(getCurrentPlayer(context))
  }

  def getResourceLocation(name: String, context: CommandContext): Option[ResourceLocation] = {
    Try(McResourceLocationArgument.getId(context, name))
      .toOption
      .flatMap(ResourceLocation(_))
  }

  def getString(name: String, context: CommandContext): Option[String] = {
    Try(StringArgumentType.getString(context, name))
      .toOption
  }
}

object ResponseType extends Enumeration {
  val NONE, MESSAGE, MESSAGE_ADMIN = Value
}
