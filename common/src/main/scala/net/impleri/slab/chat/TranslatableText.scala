package net.impleri.slab.chat

import net.minecraft.network.chat.Component

case class TranslatableText(underlying: Message.Vanilla)
    extends Message[TranslatableText] {
  override def copyAs(newVal: Message.Vanilla): TranslatableText = copy(newVal)
}

object TranslatableText {
  def apply(value: String): TranslatableText = TranslatableText(
    Component.translatable(value),
  )

  def apply(value: String, params: Any*): TranslatableText =
    TranslatableText(Component.translatable(value, params: _*))
}
