package net.impleri.slab.events

import dev.architectury.event.EventResult
import dev.architectury.event.CompoundEventResult

trait ResultHandler {
  protected def ensureResult(result: Option[EventResult]): EventResult = result.getOrElse(EventResult.pass())

  protected def ensureCompoundResult[T](result: Option[CompoundEventResult[T]]): CompoundEventResult[T] = CompoundEventResult.pass[T]()
}
