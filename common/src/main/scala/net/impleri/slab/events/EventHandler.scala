package net.impleri.slab.events

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import net.minecraft.world.item.ItemStack

trait EventHandler {
  protected def skip: EventResult = EventResult.pass()

  protected def stop: EventResult = EventResult.interruptDefault()

  protected def force: EventResult = EventResult.interruptTrue()

  protected def fail: EventResult = EventResult.interruptFalse()

  protected def failOn(
    received: Option[Boolean],
    expected: Boolean = false,
  ): EventResult = {
    if (received.contains(expected)) EventResult.interruptFalse()
    else EventResult.pass()
  }
}

trait CompoundEventHandler[T] {
  protected def skip: CompoundEventResult[T] = CompoundEventResult.pass[T]()

  protected def stop(newValue: T): CompoundEventResult[T] =
    CompoundEventResult.interruptDefault(newValue)

  protected def force(newValue: T): CompoundEventResult[T] =
    CompoundEventResult.interruptTrue(newValue)

  protected def fail(newValue: T): CompoundEventResult[T] =
    CompoundEventResult.interruptFalse(newValue)

  protected def failOn(
    received: Option[Boolean],
    value: Option[T] = None,
    expected: Boolean = false,
  ): CompoundEventResult[T] = {
    if (received.contains(expected))
      CompoundEventResult
        .interruptFalse(value.getOrElse(null.asInstanceOf[T]))
    else CompoundEventResult.pass()
  }
}

trait ItemEventHandler extends CompoundEventHandler[ItemStack]
