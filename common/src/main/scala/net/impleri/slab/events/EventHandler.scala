package net.impleri.slab.events

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import net.minecraft.world.item.ItemStack

trait EventHandler {
  protected def skip: EventResult = EventResult.pass()

  protected def stop: EventResult = EventResult.interruptDefault()

  protected def force: EventResult = EventResult.interruptTrue()

  protected def fail: EventResult = EventResult.interruptFalse()

  protected def failUnless(expected: Boolean = false)(
    received: Option[Boolean],
  ): EventResult =
    received match {
      case Some(value) if value == expected => EventResult.interruptFalse()
      case _ => EventResult.pass()
    }

    protected def failOn(
    received: Option[Boolean],
    expected: Boolean = false,
  ): EventResult = failUnless(expected)(received)
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
    response: Option[T] = None,
    expected: Boolean = false,
  ): CompoundEventResult[T] =
    received match {
      case Some(value) if value == expected => CompoundEventResult.interruptFalse(response.getOrElse(null.asInstanceOf[T]))
      case _ => CompoundEventResult.pass()
    }
}

trait ItemEventHandler extends CompoundEventHandler[ItemStack]
