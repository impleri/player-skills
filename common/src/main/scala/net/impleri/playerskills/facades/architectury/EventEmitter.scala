package net.impleri.playerskills.facades.architectury

import dev.architectury.event.Event
import dev.architectury.event.EventFactory

import java.util.function.Consumer

case class EventEmitter[T](consumer: Event[Consumer[T]]) {
  private def getInvoker = consumer.invoker()

  def register(listener: Consumer[T]): Unit = consumer.register(listener)

  def emit(event: T): Unit = getInvoker.accept(event)
}

object EventEmitter {
  private def getConsumer[T]: Event[Consumer[T]] = EventFactory.createConsumerLoop()

  def apply[T](): EventEmitter[T] = new EventEmitter[T](getConsumer)
}
