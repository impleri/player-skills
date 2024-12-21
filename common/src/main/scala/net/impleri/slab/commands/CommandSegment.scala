package net.impleri.slab.commands

import com.mojang.brigadier.builder.ArgumentBuilder

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

object CommandPermission extends Enumeration {
  case class CommandPermission(value: Int) extends super.Val

  final val MOD = CommandPermission(1)
  final val GAME_MASTER = CommandPermission(2)
  final val ADMIN = CommandPermission(3)
  final val OWNER = CommandPermission(4)
}

class CommandSegment[B <: CommandSegment.Vanilla[_], T <: CommandSegment[B, T]](
  protected val underlying: B,
) {
  protected def copyAs(nextUnderlying: B): CommandSegment[B, T] =
    new CommandSegment[B, T](
      nextUnderlying,
    )

  def executes(action: CommandAction): CommandSegment[B, T] = copyAs(
    underlying.executes(action).asInstanceOf[B],
  )

  def option[N <: CommandSegment.Vanilla[_], S <: CommandSegment[N, S]](
    subtree: CommandSegment[N, S],
  ): CommandSegment[B, T] =
    copyAs(
      underlying
        .`then`(subtree.underlying)
        .asInstanceOf[B],
    )

  def options[N <: CommandSegment.Vanilla[_], S <: CommandSegment[N, S]](
    subtrees: Seq[CommandSegment[N, S]],
  ): CommandSegment[B, T] =
    copyAs(
      subtrees.foldLeft(underlying)((parent, subtree) => parent.`then`(subtree.underlying).asInstanceOf[B]),
    )

  def requires(
    permission: CommandPermission.CommandPermission = CommandPermission.MOD,
  ): CommandSegment[B, T] =
    copyAs(
      underlying
        .requires(checkPermission(permission).asJavaPredicate)
        .asInstanceOf[B],
    )

  def requireMod(): CommandSegment[B, T] = requires(CommandPermission.MOD)

  def requireGm(): CommandSegment[B, T] = requires(
    CommandPermission.GAME_MASTER,
  )

  def requireAdmin(): CommandSegment[B, T] = requires(CommandPermission.ADMIN)

  def requireOwner(): CommandSegment[B, T] = requires(CommandPermission.OWNER)

  private def checkPermission(
    permission: CommandPermission.CommandPermission,
  ): CommandSegment.Filter = { source =>
    source.hasPermission(permission.value)
  }
}

object CommandSegment {
  type Any = CommandSegment[_, _]

  type Vanilla[T <: ArgumentBuilder[Command.Source, T]] =
    ArgumentBuilder[Command.Source, T]

  type AnyVanilla = Vanilla[_]

  private type Filter = Command.Source => Boolean
}
