package net.impleri.slab.chat

import net.impleri.slab.commands.Command

case class ListMessage(
  override val underlying: Message.Vanilla,
  children: Seq[Message.Any],
) extends Message[ListMessage] {
  override def copyAs(newVal: Message.Vanilla): ListMessage =
    copy(underlying = newVal)

  override def sendSuccess(context: Command.Context): Unit = {
    super.sendSuccess(context)
    sendChildren(context)
  }

  override def sendSuccessWithAdmins(context: Command.Context): Unit = {
    super.sendSuccessWithAdmins(context)
    sendChildren(context)
  }

  override def sendGeneric(context: Command.Context): Unit = {
    super.sendGeneric(context)
    sendChildren(context)
  }

  override def sendFailure(context: Command.Context): Unit = {
    super.sendFailure(context)
    sendChildren(context)
  }

  private def sendChildren(context: Command.Context): Unit = {
    children.foreach(_.sendGeneric(context))
  }
}

object ListMessage {
  def apply(from: Message[_], children: Seq[Message[_]]): ListMessage = {
    ListMessage(from.mutableOutput, children)
  }
}
