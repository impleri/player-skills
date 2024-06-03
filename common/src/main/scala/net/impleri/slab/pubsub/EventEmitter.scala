package net.impleri.slab.pubsub

import dev.architectury.event.Event
import dev.architectury.event.EventFactory

import java.util.function.Consumer

case class EventEmitter[T](underlying: EventEmitter.Vanilla[T]) {
  private def getInvoker = underlying.invoker()

  def register(listener: Consumer[T]): Unit = underlying.register(listener)

  def emit(event: T): Unit = getInvoker.accept(event)
}

object EventEmitter {
  type Vanilla[T] = Event[Consumer[T]]

  private def getConsumer[T]: Vanilla[T] = EventFactory.createConsumerLoop()

  def apply[T](): EventEmitter[T] = new EventEmitter[T](getConsumer)
}
