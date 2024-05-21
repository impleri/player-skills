package net.impleri.playerskills.utils

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult

trait EventUtils {
  protected def failOn(received: Option[Boolean], expected: Boolean = false): EventResult = {
    if (received.contains(expected)) EventResult.interruptFalse() else EventResult.pass()
  }

  protected def failCompoundOn[T >: Null](
    received: Option[Boolean],
    value: Option[T] = None,
    expected: Boolean = false,
  ): CompoundEventResult[T] = {
    if (received.contains(expected)) CompoundEventResult.interruptFalse(value.orNull) else CompoundEventResult.pass()
  }
}
