package net.impleri.slab.commands

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

object CommandPermission extends Enumeration {
  case class CommandPermission(value: Int) extends super.Val

  final val MOD = CommandPermission(1)
  final val GAME_MASTER = CommandPermission(2)
  final val ADMIN = CommandPermission(3)
  final val OWNER = CommandPermission(4)
}

class CommandSegment[B <: CommandBuilder[_], T <: CommandSegment[B, _]](protected val underlying: B) {
  protected def copyAs[N <: CommandBuilder[_]](nextUnderlying: N): CommandSegment[N, _] = {
    new CommandSegment[N, _](
      nextUnderlying,
    )
  }

  def executes(action: CommandAction[_]): CommandSegment[B, _] = copyAs(underlying.executes(action))

  def option[S <: CommandBuilder[_]](subtree: CommandSegment[S, _]): CommandSegment[B, _] = {
    copyAs(underlying
      .`then`(subtree.underlying),
    )
  }

  def requires(permission: CommandPermission.CommandPermission = CommandPermission.MOD): CommandSegment[B, _] = {
    copyAs(
      underlying.requires(checkPermission(permission).asJavaPredicate),
    )
  }

  def requireMod(): CommandSegment[B, _] = requires(CommandPermission.MOD)

  def requireGm(): CommandSegment[B, _] = requires(CommandPermission.GAME_MASTER)

  def requireAdmin(): CommandSegment[B, _] = requires(CommandPermission.ADMIN)

  def requireOwner(): CommandSegment[B, _] = requires(CommandPermission.OWNER)

  private def checkPermission(permission: CommandPermission.CommandPermission): CommandFilter = {
    source => source.hasPermission(permission.value)
  }
}

object CommandSegment {
  type Any = CommandSegment[_, _]
}
