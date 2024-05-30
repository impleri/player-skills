package net.impleri.slab.chat

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

case class StaticText(underlying: MutableComponent) extends Message[StaticText] {
  override def copyAs(newVal: MutableComponent): StaticText = copy(newVal)
}

object StaticText {
  def apply(value: String): StaticText = StaticText(Component.literal(value))
}
