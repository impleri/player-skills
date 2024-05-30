package net.impleri.slab.chat

import net.impleri.slab.commands.CommandContext
import net.minecraft.network.chat.MutableComponent

case class ListMessage(protected val underlying: MutableComponent, children: Seq[Message[_]])
  extends Message[ListMessage] {
  override def copyAs(newVal: MutableComponent): ListMessage = copy(underlying = newVal)

  override protected[commands] def sendSuccess(context: CommandContext): Unit = {
    super.sendSuccess(context)
    sendChildren(context)
  }

  override protected[commands] def sendSuccessWithAdmins(context: CommandContext): Unit = {
    super.sendSuccessWithAdmins(context)
    sendChildren(context)
  }

  override protected[commands] def sendGeneric(context: CommandContext): Unit = {
    super.sendGeneric(context)
    sendChildren(context)
  }

  override protected[commands] def sendFailure(context: CommandContext): Unit = {
    super.sendFailure(context)
    sendChildren(context)
  }

  private def sendChildren(context: CommandContext): Unit = {
    children.foreach(_.sendGeneric(context))
  }
}

object ListMessage {
  def apply(from: Message[_], children: Seq[Message[_]]): ListMessage = {
    ListMessage(from.underlying, children)
  }
}
