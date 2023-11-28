package net.impleri.playerskills.client

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.facades.architectury.EventEmitter

import java.util.function.Consumer

class EventHandlerSpec extends BaseSpec {
  private val eventEmitterMock = mock[EventEmitter[ClientSkillsUpdatedEvent]]
  private val testUnit: EventHandler = EventHandler(eventEmitterMock)

  "EventHandler.onSkillsUpdate" should "proxies register method" in {
    val consumerMock = mock[Consumer[ClientSkillsUpdatedEvent]]

    testUnit.onSkillsUpdate(consumerMock)

    eventEmitterMock.register(consumerMock) wasCalled once
  }

  "EventHandler.emitSkillsUpdated" should "proxies register method" in {
    val prev = List.empty
    val next = List(
      mock[Skill[Boolean]],
    )
    testUnit.emitSkillsUpdated(next, prev, false)

    eventEmitterMock.emit(any[ClientSkillsUpdatedEvent]) wasCalled once
  }

  "EventHandler.apply" should "return a valid class for clients" in {
    EventHandler().isInstanceOf[EventHandler] should be(true)
  }
}
