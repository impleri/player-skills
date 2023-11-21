package net.impleri.playerskills.server

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.architectury.EventEmitter
import net.impleri.playerskills.facades.minecraft.Player

import java.util.function.Consumer

class EventHandlerSpec extends BaseSpec {
  private val eventEmitterMock = mock[EventEmitter[SkillChangedEvent[_]]]
  private val testUnit: EventHandler = EventHandler(eventEmitterMock)

  private val playerMock: Player[_] = mock[Player[_]]
  private val skillMock: Skill[_] = mock[Skill[_]]

  "EventHandler.onSkillChanged" should "proxies register method" in {
    val consumerMock: Consumer[SkillChangedEvent[_]] = mock[Consumer[SkillChangedEvent[_]]]

    testUnit.onSkillChanged(consumerMock)

    eventEmitterMock.register(consumerMock) wasCalled once
  }

  "EventHandler.emitSkillChanged" should "proxies register method" in {
    testUnit.emitSkillChanged(playerMock, skillMock, None)

    eventEmitterMock.emit(any[SkillChangedEvent[_]]) wasCalled once
  }

  "EventHandler.apply" should "return a valid class" in {
    EventHandler().isInstanceOf[EventHandler] should be(true)
  }
}
