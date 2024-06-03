package net.impleri.slab.chat

import net.minecraft.network.chat.Component

case class StaticText(underlying: Message.Vanilla) extends Message[StaticText] {
  override def copyAs(newVal: Message.Vanilla): StaticText = copy(newVal)
}

object StaticText {
  def apply(value: String): StaticText = StaticText(Component.literal(value))
}
