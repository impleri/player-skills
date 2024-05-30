package net.impleri.slab.chat

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

case class TranslatableText(underlying: MutableComponent) extends Message[TranslatableText] {
  override def copyAs(newVal: MutableComponent): TranslatableText = copy(newVal)
}

object TranslatableText {
  def apply(value: String): TranslatableText = TranslatableText(Component.translatable(value))

  def apply(value: String, params: Object*): TranslatableText = {
    TranslatableText(Component.translatable(value, params: _*))
  }
}
