package net.impleri.playerskills.server.bindings

import dev.architectury.event.Event
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Item
import net.impleri.playerskills.facades.minecraft.Entity
import net.impleri.playerskills.restrictions.item.ItemRestrictionOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.player.{Player => MinecraftPlayer}

class EntityEventsSpec extends BaseSpec {
  private val mockOps = mock[ItemRestrictionOps]
  private val mockEvent = mock[Event[EntityEvent.LivingHurt]]
  private val mockLogger = mock[PlayerSkillsLogger]

  private val testUnit = EntityEvents(mockOps, mockEvent, mockLogger, mockLogger)

  private val mockEntity = mock[Entity[_]]
  private val mockAttacker = mock[Entity[_]]
  private val mockPlayer = mock[Player[MinecraftPlayer]]
  mockAttacker.asPlayer[MinecraftPlayer] returns mockPlayer
  private val mockItem = mock[Item]
  mockPlayer.getItemInMainHand returns mockItem

  "EntityEvents.registerEvents" should "register event handlers" in {
    testUnit.registerEvents()

    mockEvent.register(*) wasCalled once
  }

  "EntityEvents.beforePlayerAttack" should "interrupt the event if the player's item is unusable" in {
    mockAttacker.isPlayer returns true

    mockOps.isHarmful(mockPlayer, mockItem) returns false

    val result = testUnit.beforePlayerAttack(mockEntity, mockAttacker)

    result.isFalse shouldBe true
    result.interruptsFurtherEvaluation() shouldBe true
  }

  it should "do nothing to the event if item is usable" in {
    mockAttacker.isPlayer returns true

    mockOps.isHarmful(mockPlayer, mockItem) returns true

    testUnit.beforePlayerAttack(mockEntity, mockAttacker) shouldBe EventResult.pass()
  }

  it should "do nothing to the event if attacker is not a player" in {
    mockAttacker.isPlayer returns false

    testUnit.beforePlayerAttack(mockEntity, mockAttacker) shouldBe EventResult.pass()

    mockOps.isHarmful(mockPlayer, mockItem) wasNever called

  }
}
